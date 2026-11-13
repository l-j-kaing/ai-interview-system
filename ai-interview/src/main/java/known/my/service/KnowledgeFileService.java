package known.my.service;

import jakarta.validation.Valid;
import known.my.pojo.dto.ChunkByPunctuationDTO;
import known.my.pojo.dto.VectorDTO;
import known.my.pojo.vo.ChunkByPunctuationVO;
import known.my.pojo.vo.KnowledgeFileInfoVO;
import known.my.pojo.vo.KnowledgeFileUploadVO;
import org.springframework.web.multipart.MultipartFile;

public interface KnowledgeFileService {
    /**
     * 加载知识文件
     */
    KnowledgeFileUploadVO uploadKnowledgeFile(
             MultipartFile file,
             String category,
             String visibility,
             Long userId
     );

    /**
    * 查询用户指定知识文件的信息
    */
    KnowledgeFileInfoVO getKnowledgeInfo(Long userId, String fileId);

    ChunkByPunctuationVO chunkByPunctuation(ChunkByPunctuationDTO chunkByPunctuationDTO);

    void vectorize(@Valid VectorDTO vectorDTO);
}
