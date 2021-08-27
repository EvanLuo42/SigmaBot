package cn.phakel.sigma.util;

import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;

/**
 * @author EvanLuo42
 * @date 2021/8/25 8:44 下午
 */
public class Sender {
    public static SendPhoto imageSender(String url, Long chatId) {
        SendPhoto sendPhotoRequest = new SendPhoto();
        sendPhotoRequest.setChatId(String.valueOf(chatId));
        sendPhotoRequest.setPhoto(new InputFile(url));

        return sendPhotoRequest;
    }
}
