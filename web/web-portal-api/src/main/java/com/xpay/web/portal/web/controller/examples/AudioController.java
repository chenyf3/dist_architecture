package com.xpay.web.portal.web.controller.examples;

import com.xpay.common.statics.annotations.Permission;
import com.xpay.common.statics.result.RestResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.CodeUtil;
import com.xpay.facade.message.dto.TTSDto;
import com.xpay.facade.message.service.TTSFacade;
import com.xpay.web.portal.web.controller.BaseController;
import com.xpay.web.portal.web.vo.examples.TTSVo;
import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/examples/audio")
public class AudioController extends BaseController {

    @DubboReference
    TTSFacade ttsFacade;

    @Permission("examples:audio:transfer")
    @RequestMapping("transferAudio")
    public RestResult<String> transferAudio(@RequestBody @Valid TTSVo ttsVo) {
        TTSDto audioDto = BeanUtil.newAndCopy(ttsVo, TTSDto.class);
        byte[] audioBytes = ttsFacade.onlineAudioTransfer(audioDto);
        return RestResult.success(CodeUtil.base64Encode(audioBytes));
    }
}
