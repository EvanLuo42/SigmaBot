package cn.phakel.sigma;

import cn.phakel.sigma.config.SignConfig;
import cn.phakel.sigma.util.SimpleConfig;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;

/**
 * @author EvanLuo42
 * @date 2021/8/25 7:38 下午
 */
public class Main {
    public static SimpleConfig<SignConfig> getSignConfig() {
        return new SimpleConfig<SignConfig>(new File("./config"), SignConfig.class);
    }

    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new SigmaBot());

            getSignConfig().setConfigFileName("sign.json");
            getSignConfig().saveDefault();
            getSignConfig().reloadConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
