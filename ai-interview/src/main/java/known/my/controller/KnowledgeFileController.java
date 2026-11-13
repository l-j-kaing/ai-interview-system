package known.my.controller;

import jakarta.validation.Valid;
import known.my.common.context.BaseContext;
import known.my.common.result.Result;
import known.my.pojo.dto.ChunkByPunctuationDTO;
import known.my.pojo.dto.KnowledgeFileUploadDTO;
import known.my.pojo.dto.VectorDTO;
import known.my.pojo.vo.ChunkByPunctuationVO;
import known.my.pojo.vo.KnowledgeFileInfoVO;
import known.my.pojo.vo.KnowledgeFileUploadVO;
import known.my.service.KnowledgeFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/knowledge")
@Slf4j
public class KnowledgeFileController {
    @Autowired
    private KnowledgeFileService knowledgeFileService;
    /**
     * 加载知识文件
     */
    @PostMapping("/upload")
    public Result<KnowledgeFileUploadVO> uploadKnowledgeFile(
            @Valid @ModelAttribute KnowledgeFileUploadDTO knowledgeFileUploadDTO
    ) {
        KnowledgeFileUploadVO knowledgeFileUploadVO = knowledgeFileService.uploadKnowledgeFile(
                knowledgeFileUploadDTO.getFile(),
                knowledgeFileUploadDTO.getCategory(),
                knowledgeFileUploadDTO.getVisibility(),
                BaseContext.getCurrentId()

        );
        return Result.success(knowledgeFileUploadVO);
    }
    /**
     * 获取知识文件上传状态
     */
    @GetMapping("/info/{fileId}")
    public Result<KnowledgeFileInfoVO> getKnowledgeInfo(@PathVariable String fileId) {
        KnowledgeFileInfoVO knowledgeFileInfoVO =
                knowledgeFileService.getKnowledgeInfo(BaseContext.getCurrentId(), fileId);
        return Result.success(knowledgeFileInfoVO);
    }
    /**
     * 按照用户分块方案进行chunk
     */
    @PostMapping("/chunk/punctuation")
    public Result<ChunkByPunctuationVO> chunkByPunctuation(@Valid @RequestBody ChunkByPunctuationDTO chunkByPunctuationDTO) {
        ChunkByPunctuationVO chunkByPunctuationVO = knowledgeFileService.chunkByPunctuation(chunkByPunctuationDTO);
        return Result.success(chunkByPunctuationVO);
    }
    /**
     * 进行向量化入库
     */
    @PostMapping("/vectorize")
    public Result<Void> vectorize(@Valid @RequestBody VectorDTO vectorDTO) {
        knowledgeFileService.vectorize(vectorDTO);
        return Result.success();
    }
}
