package known.my.service.impl;

import known.my.client.PythonRagClient;
import known.my.common.constant.FileStatusConstants;
import known.my.common.context.BaseContext;
import known.my.common.exception.BaseException;
import known.my.common.result.ResultCode;
import known.my.mapper.KnowledgeFileMapper;
import known.my.pojo.dto.ChunkByPunctuationDTO;
import known.my.pojo.dto.VectorDTO;
import known.my.pojo.entity.KnowledgeFile;
import known.my.pojo.enums.StorageTypeEnum;
import known.my.pojo.request.RagChunkRequest;
import known.my.pojo.request.RagVectorRequest;
import known.my.pojo.response.RagChunkResponse;
import known.my.pojo.vo.ChunkByPunctuationVO;
import known.my.pojo.vo.KnowledgeFileInfoVO;
import known.my.pojo.vo.KnowledgeFileUploadVO;
import known.my.service.FileStorageService;
import known.my.service.KnowledgeFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeFileServiceImpl implements KnowledgeFileService {
    private final FileStorageService fileStorageService;
    private final KnowledgeFileMapper knowledgeFileMapper;
    private final PythonRagClient pythonRagClient;

    /**
     * 保存知识文件并创建对应的数据库记录。
     */
    @Override
    @Transactional
    public KnowledgeFileUploadVO uploadKnowledgeFile(MultipartFile file, String category, String visibility, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new BaseException(ResultCode.PARAM_ERROR,"上传文件不能为空");
        }
        if (userId == null) {
            throw new BaseException(ResultCode.FORBIDDEN,"当前用户不能为空");
        }
        if (category == null || category.isBlank()) {
            throw new BaseException(ResultCode.PARAM_ERROR,"知识文件分类不能为空");
        }
        if (!"private".equals(visibility) && !"public".equals(visibility)) {
            throw new BaseException(ResultCode.PARAM_ERROR,"可见性必须是 private 或 public");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new BaseException(ResultCode.PARAM_ERROR,"上传文件名不能为空");
        }
        String fileId = UUID.randomUUID().toString();
        String fileType = file.getContentType();
        if (fileType == null || fileType.isBlank()) {
            fileType = "application/octet-stream";
        }
        long fileSize = file.getSize();

        //保存文件
        String storagePath = fileStorageService.save(file, userId, fileId);
        //数据库插入文件信息
        KnowledgeFile knowledgeFile = KnowledgeFile.builder()
                .userId(userId)
                .fileId(fileId)
                .fileName(fileName)
                .fileType(fileType)
                .fileSize(fileSize)
                .category(category)
                .visibility(visibility)
                .status(FileStatusConstants.UPLOAD_SUCCESS)
                .storagePath(storagePath)
                .storageType(StorageTypeEnum.LOCAL.getValue())
                .build();
        if(knowledgeFileMapper.insertKnowledgeFile(knowledgeFile) != 1){
            // 删除已保存的文件
            fileStorageService.delete(storagePath);
            throw new BaseException(ResultCode.INTERNAL_ERROR,"保存文件信息失败");
        }
        return KnowledgeFileUploadVO.builder()
                .fileId(knowledgeFile.getFileId())
                .fileSize(knowledgeFile.getFileSize())
                .fileName(knowledgeFile.getFileName())
                .build();
    }

    /**
     * 查询用户指定知识文件的信息。
     */
    @Override
    @Transactional(readOnly = true)
    public KnowledgeFileInfoVO getKnowledgeInfo(Long userId, String fileId) {
        if (userId == null) {
            throw new BaseException(ResultCode.UNAUTHORIZED, "未登录或登录状态已失效");
        }
        if (fileId == null || fileId.isBlank()) {
            throw new BaseException(ResultCode.PARAM_ERROR, "文件ID不能为空");
        }

        KnowledgeFile knowledgeFile =
                knowledgeFileMapper.selectKnowledgeFileInfoByUserIdAndFileId(userId, fileId);
        if (knowledgeFile == null) {
            throw new BaseException(ResultCode.NOT_FOUND, "知识文件不存在");
        }
        return KnowledgeFileInfoVO.builder()
                .id(knowledgeFile.getId())
                .userId(knowledgeFile.getUserId())
                .fileId(knowledgeFile.getFileId())
                .fileName(knowledgeFile.getFileName())
                .fileType(knowledgeFile.getFileType())
                .fileSize(knowledgeFile.getFileSize())
                .category(knowledgeFile.getCategory())
                .visibility(knowledgeFile.getVisibility())
                .status(knowledgeFile.getStatus())
                .build();
    }
    /**
     * 根据分段上传知识文件
     */
    @Override
    public ChunkByPunctuationVO chunkByPunctuation(ChunkByPunctuationDTO chunkByPunctuationDTO) {
        //根据文件id查询文件信息
        String fileId = chunkByPunctuationDTO.getFileId();
        if(fileId == null || fileId.isBlank()){
            throw new BaseException(ResultCode.PARAM_ERROR, "文件ID不能为空");
        }
        KnowledgeFile knowledgeFile = knowledgeFileMapper.selectKnowledgeFileByUserIdAndFileId(BaseContext.getCurrentId(), fileId);
        if (knowledgeFile == null) {
            throw new BaseException(ResultCode.NOT_FOUND, "文件不存在");
        }
        String storagePath = knowledgeFile.getStoragePath();
        if(storagePath == null || storagePath.isBlank())
            throw new BaseException(ResultCode.NOT_FOUND, "文件存储路径不能为空");
        RagChunkRequest ragChunkRequest = RagChunkRequest.builder()
                .chunkOverlap(chunkByPunctuationDTO.getChunkOverlap())
                .chunkSize(chunkByPunctuationDTO.getChunkSize())
                .filePath(storagePath)
                .splitPunctuations(chunkByPunctuationDTO.getSplitPunctuations())
                .fileId(fileId)
                .userId(BaseContext.getCurrentId().intValue())
                .build();
        RagChunkResponse ragChunkResponse = pythonRagClient.chunkKnowledgeFile(ragChunkRequest);
        if(ragChunkResponse == null){
            KnowledgeFile kl = KnowledgeFile.builder()
                    .fileId(fileId)
                    .status(FileStatusConstants.CHUNKING_FAILED)
                    .userId(BaseContext.getCurrentId())
                    .build();
            knowledgeFileMapper.updateKnowledgeFile(kl);
            throw new BaseException(ResultCode.INTERNAL_ERROR, "分段上传失败");
        }
        KnowledgeFile kl = KnowledgeFile.builder()
                .fileId(fileId)
                .userId(BaseContext.getCurrentId())
                .status(FileStatusConstants.CHUNKING_SUCCESS)
                .build();
        knowledgeFileMapper.updateKnowledgeFile(kl);
        return ChunkByPunctuationVO.builder()
                .chunks(ragChunkResponse.getChunks())
                .category(knowledgeFile.getCategory())
                .build();


    }
    /**
     * 根据分段上传知识文件
     */
    @Override
    public void vectorize(VectorDTO vectorDTO) {
        //根据文件id查询文件信息
        KnowledgeFile knowledgeFile = knowledgeFileMapper.selectKnowledgeFileByUserIdAndFileId(BaseContext.getCurrentId(), vectorDTO.getFileId());
        if (knowledgeFile == null) {
            throw new BaseException(ResultCode.NOT_FOUND, "文件不存在");
        }
        RagVectorRequest ragVectorRequest = RagVectorRequest.builder()
                .fileId(vectorDTO.getFileId())
                .chunks(vectorDTO.getChunks())
                .category(vectorDTO.getCategory())
                .userId(BaseContext.getCurrentId().intValue())
                .visibility(knowledgeFile.getVisibility())
                .build();
        //等待agent处理
       try{
           pythonRagClient.vectorizeKnowledgeChunks(ragVectorRequest);
       } catch (Exception e) {
           //更新文件状态为向量化失败
           KnowledgeFile kl = KnowledgeFile.builder()
                   .fileId(vectorDTO.getFileId())
                   .userId(BaseContext.getCurrentId())
                   .status(FileStatusConstants.VECTORIZING_FAILED)
                   .build();
           knowledgeFileMapper.updateKnowledgeFile(kl);
           throw new BaseException(ResultCode.INTERNAL_ERROR, "向量化失败");
       }
       KnowledgeFile kl = KnowledgeFile.builder()
               .fileId(vectorDTO.getFileId())
               .userId(BaseContext.getCurrentId())
               .status(FileStatusConstants.VECTORIZING_SUCCESS)
               .build();
       knowledgeFileMapper.updateKnowledgeFile(kl);

    }
}
