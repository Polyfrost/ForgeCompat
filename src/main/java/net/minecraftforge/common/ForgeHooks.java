package net.minecraftforge.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Formatting;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForgeHooks {
    static final Pattern URL_PATTERN = Pattern.compile("((?:[a-z0-9]{2,}:\\/\\/)?(?:(?:[0-9]{1,3}\\.){3}[0-9]{1,3}|(?:[-\\w_\\.]{1,}\\.[a-z]{2,}?))(?::[0-9]{1,5})?.*?(?=[!\"\u00A7 \n]|$))", Pattern.CASE_INSENSITIVE);

    public static Text newChatWithLinks(String string) {
        return newChatWithLinks(string, true);
    }

    public static Text newChatWithLinks(String string, boolean allowMissingHeader) {
        Text ichat = null;
        Matcher matcher = URL_PATTERN.matcher(string);
        int lastEnd = 0;
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String part = string.substring(lastEnd, start);
            if (!part.isEmpty()) ichat = append(ichat, new LiteralText(part));
            lastEnd = end;
            String url = string.substring(start, end);
            Text link = new LiteralText(url);
            try {
                if (new URI(url).getScheme() == null) {
                    if (!allowMissingHeader) {
                        ichat = append(ichat, link);
                        continue;
                    }
                    url = "http://" + url;
                }
            } catch (Exception e) {
                ichat = append(ichat, link);
                continue;
            }
            link.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
            link.getStyle().setUnderlined(true);
            link.getStyle().setColor(Formatting.BLUE);
            ichat = append(ichat, link);
        }
        String end = string.substring(lastEnd);
        if (ichat == null) ichat = new LiteralText(end);
        else if (!end.isEmpty()) ichat.append(new LiteralText(end));
        return ichat;
    }

    private static Text append(Text chat, Text part) {
        return chat == null ? part : chat.append(part);
    }

    public static int getTotalArmorValue(PlayerEntity player) {
        return player.getArmorProtection();
    }

    public static void onLivingJump(LivingEntity entity) {
    }

    public static boolean onPlayerAttackTarget(PlayerEntity player, Entity target) {
        return !MinecraftForge.EVENT_BUS.post(new AttackEntityEvent(player, target));
    }
}
