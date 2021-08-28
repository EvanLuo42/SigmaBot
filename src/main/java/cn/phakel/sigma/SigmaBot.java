package cn.phakel.sigma;

import cn.phakel.sigma.config.SignConfig;
import cn.phakel.sigma.model.Github;
import cn.phakel.sigma.model.Setu;
import cn.phakel.sigma.model.Weather;
import cn.phakel.sigma.util.Logger;
import cn.phakel.sigma.util.Sender;
import com.google.gson.Gson;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static cn.phakel.sigma.Config.*;

/**
 * @author EvanLuo42
 * @date 2021/8/25 5:22 下午
 */
public class SigmaBot extends AbilityBot {
    protected SigmaBot() {
        super(BOT_TOKEN, BOT_NAME);
    }

    @Override
    public long creatorId() {
        return 1082731192;
    }

    public Ability sendSetu() {
        return Ability
                .builder()
                .name("setu")
                .info("Send setu")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    Logger.log(ctx.user().getUserName() + " used command /setu");
                    try {
                        var client = HttpClient.newHttpClient();
                        var url = "https://api.lolicon.app/setu/v2?r18=2";
                        var request = HttpRequest.newBuilder()
                                .GET()
                                .uri(URI.create(url))
                                .build();
                        var body = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
                        var gson = new Gson();
                        var res = gson.fromJson(body, Setu.class);

                        execute(Sender.imageSender(res.data[0].urls.original, ctx.chatId()));
                        silent.send("标题:"
                                + res.data[0].title
                                + "\n作品pid:"
                                + res.data[0].pid
                                + "\n作者uid:"
                                + res.data[0].uid, ctx.chatId());

                    } catch (Exception e) {
                        e.printStackTrace();
                        silent.send("API出现问题，请联系管理员解决。", ctx.chatId());
                    }
                }).build();
    }

    public Ability sendGithubRepo() {
        return Ability
                .builder()
                .name("repo")
                .info("Get Github repository")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    Logger.log(ctx.user().getUserName() + " used command /repo");
                    try {
                        var client = HttpClient.newHttpClient();
                        var request = HttpRequest.newBuilder()
                                .GET()
                                .uri(URI.create("https://api.github.com/repos/" + ctx.firstArg()))
                                .build();
                        var body = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
                        var gson = new Gson();
                        var res = gson.fromJson(body, Github.class);

                        silent.send("链接:" + res.html_url, ctx.chatId());
                    } catch (Exception e) {
                        Logger.error(e.toString());
                        silent.send("未知错误", ctx.chatId());
                    }
                }).build();
    }

    public Ability sendWeather() {
        return Ability
                .builder()
                .name("weather")
                .info("Get the weather")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    Logger.log(ctx.user().getUserName() + " used command /weather");
                    try {
                        var client = HttpClient.newHttpClient();
                        var url = new StringBuilder(Config.NOWAPIURL)
                                .append("?app=weather.today&cityNm=")
                                .append(ctx.firstArg())
                                .append("&appkey=")
                                .append(NOWAPIAPPKEY)
                                .append("&sign=")
                                .append(NOWAPISIGN)
                                .append("&format=json");

                        var request = HttpRequest.newBuilder()
                                .GET()
                                .uri(URI.create(String.valueOf(url)))
                                .build();
                        var body = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
                        var gson = new Gson();
                        var res = gson.fromJson(body, Weather.class);

                        if (res.success.equals("1")) {
                            execute(Sender.imageSender(res.result.weather_icon, ctx.chatId()));
                            silent.send("天气:"
                                    + res.result.weather
                                    + "\n温度:"
                                    + res.result.temperature_curr
                                    + "\n空气质量:"
                                    + res.result.aqi, ctx.chatId());
                        } else {
                            Logger.warning("City does not exist");
                            silent.send("城市不存在", ctx.chatId());
                        }
                    } catch (Exception e) {
                        Logger.error(e.toString());
                        silent.send("未知错误", ctx.chatId());
                    }
                }).build();
    }

    public Ability sign() {
        return Ability
                .builder()
                .name("sign")
                .info("Sign")
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
                .action(ctx -> {
                    Logger.log(ctx.user().getUserName() + " used command /sign");
                    DateTimeFormatter fmDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDateTime now = LocalDateTime.now();

                    if (Main.getSignConfig().get().sign.containsKey(ctx.chatId())) {
                        SignConfig.User user = Main.getSignConfig().get().sign.get(ctx.chatId());

                        if (!user.lastSignedDate.equals(now.format(fmDate))) {
                            user.score++;
                            user.lastSignedDate = now.format(fmDate);
                            silent.send("Sign successfully. Score +1", ctx.chatId());
                            Logger.log( ctx.chatId() + " signed");
                        } else {
                            silent.send("You have signed today.", ctx.chatId());
                        }
                    } else {
                        Main.getSignConfig().get().sign.put(ctx.chatId(), new SignConfig.User(1, now.format(fmDate)));
                        silent.send("Sign successfully. Score +1", ctx.chatId());
                        Logger.log( ctx.chatId() + " signed");
                    }

                    Main.getSignConfig().saveConfig();

                    Logger.log(Main.getSignConfig().get().sign.get(1082731192L).toString());
                }).build();
    }
}