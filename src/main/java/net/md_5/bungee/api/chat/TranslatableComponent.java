package net.md_5.bungee.api.chat;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TranslatableComponent
        extends BaseComponent {
    private final ResourceBundle locales = ResourceBundle.getBundle("mojang-translations/en_US");
    private final Pattern format = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");
    private String translate;
    private List<BaseComponent> with;

    public TranslatableComponent(TranslatableComponent original) {
        super(original);
        this.setTranslate(original.getTranslate());
        for (BaseComponent baseComponent : original.getWith()) {
            this.with.add(baseComponent.duplicate());
        }
    }

    public /* varargs */ TranslatableComponent(String translate, Object... with) {
        this.setTranslate(translate);
        ArrayList<BaseComponent> temp = new ArrayList<BaseComponent>();
        for (Object w : with) {
            if (w instanceof String) {
                temp.add(new TextComponent((String) w));
                continue;
            }
            temp.add((BaseComponent) w);
        }
        this.setWith(temp);
    }

    public TranslatableComponent() {
    }

    @Override
    public BaseComponent duplicate() {
        return new TranslatableComponent(this);
    }

    public void addWith(String text) {
        this.addWith(new TextComponent(text));
    }

    public void addWith(BaseComponent component) {
        if (this.with == null) {
            this.with = new ArrayList<BaseComponent>();
        }
        component.parent = this;
        this.with.add(component);
    }

    @Override
    protected void toPlainText(StringBuilder builder) {
        try {
            String trans = this.locales.getString(this.translate);
            Matcher matcher = this.format.matcher(trans);
            int position = 0;
            int i = 0;
            while (matcher.find(position)) {
                int pos = matcher.start();
                if (pos != position) {
                    builder.append(trans, position, pos);
                }
                position = matcher.end();
                String formatCode = matcher.group(2);
                switch (formatCode.charAt(0)) {
                    case 'd':
                    case 's': {
                        String withIndex = matcher.group(1);
                        this.with.get(withIndex != null ? Integer.parseInt(withIndex) - 1 : i++).toPlainText(builder);
                        break;
                    }
                    case '%': {
                        builder.append('%');
                    }
                }
            }
            if (trans.length() != position) {
                builder.append(trans.substring(position));
            }
        } catch (MissingResourceException e) {
            builder.append(this.translate);
        }
        super.toPlainText(builder);
    }

    @Override
    protected void toLegacyText(StringBuilder builder) {
        try {
            String trans = this.locales.getString(this.translate);
            Matcher matcher = this.format.matcher(trans);
            int position = 0;
            int i = 0;
            while (matcher.find(position)) {
                int pos = matcher.start();
                if (pos != position) {
                    this.addFormat(builder);
                    builder.append(trans, position, pos);
                }
                position = matcher.end();
                String formatCode = matcher.group(2);
                switch (formatCode.charAt(0)) {
                    case 'd':
                    case 's': {
                        String withIndex = matcher.group(1);
                        this.with.get(withIndex != null ? Integer.parseInt(withIndex) - 1 : i++).toLegacyText(builder);
                        break;
                    }
                    case '%': {
                        this.addFormat(builder);
                        builder.append('%');
                    }
                }
            }
            if (trans.length() != position) {
                this.addFormat(builder);
                builder.append(trans.substring(position));
            }
        } catch (MissingResourceException e) {
            this.addFormat(builder);
            builder.append(this.translate);
        }
        super.toLegacyText(builder);
    }

    private void addFormat(StringBuilder builder) {
        builder.append(this.getColor());
        if (this.isBold()) {
            builder.append(ChatColor.BOLD);
        }
        if (this.isItalic()) {
            builder.append(ChatColor.ITALIC);
        }
        if (this.isUnderlined()) {
            builder.append(ChatColor.UNDERLINE);
        }
        if (this.isStrikethrough()) {
            builder.append(ChatColor.STRIKETHROUGH);
        }
        if (this.isObfuscated()) {
            builder.append(ChatColor.MAGIC);
        }
    }

    public ResourceBundle getLocales() {
        return this.locales;
    }

    public Pattern getFormat() {
        return this.format;
    }

    public String getTranslate() {
        return this.translate;
    }

    public void setTranslate(String translate) {
        this.translate = translate;
    }

    public List<BaseComponent> getWith() {
        return this.with;
    }

    public void setWith(List<BaseComponent> components) {
        for (BaseComponent component : components) {
            component.parent = this;
        }
        this.with = components;
    }

    @Override
    public String toString() {
        return "TranslatableComponent(locales=" + this.getLocales() + ", format=" + this.getFormat() + ", translate=" + this.getTranslate() + ", with=" + this.getWith() + ")";
    }
}

