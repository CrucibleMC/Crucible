package io.github.crucible.nbt;

import net.minecraft.nbt.*;
import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Crucible_JsonToNBT {
    private static final Pattern DOUBLE_PATTERN_NOSUFFIX = Pattern.compile("[-+]?(?:[0-9]+[.]|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?", 2);
    private static final Pattern DOUBLE_PATTERN = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?d", 2);
    private static final Pattern FLOAT_PATTERN = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?f", 2);
    private static final Pattern BYTE_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)b", 2);
    private static final Pattern LONG_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)l", 2);
    private static final Pattern SHORT_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)s", 2);
    private static final Pattern INT_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)");
    private final String string;
    private int cursor;

    public static NBTTagCompound getTagFromJson(String jsonString) throws NBTException {
        return (new Crucible_JsonToNBT(jsonString)).readSingleStruct();
    }

    NBTTagCompound readSingleStruct() throws NBTException {
        NBTTagCompound nbttagcompound = this.readStruct();
        this.skipWhitespace();

        if (this.canRead()) {
            ++this.cursor;
            throw this.exception("Trailing data found");
        } else {
            return nbttagcompound;
        }
    }

    Crucible_JsonToNBT(String stringIn) {
        this.string = stringIn;
    }

    protected String readKey() throws NBTException {
        this.skipWhitespace();

        if (!this.canRead()) {
            throw this.exception("Expected key");
        } else {
            return this.peek() == '"' ? this.readQuotedString() : this.readString();
        }
    }

    private NBTException exception(String message) {
        return new Crucible_NBTException(message, this.string, this.cursor);
    }

    protected NBTBase readTypedValue() throws NBTException {
        this.skipWhitespace();

        if (this.peek() == '"') {
            return new NBTTagString(this.readQuotedString());
        } else {
            String type = this.readString();

            if (type.isEmpty()) {
                throw this.exception("Expected value");
            } else {
                return this.type(type);
            }
        }
    }

    private NBTBase type(String stringIn) {
        try {
            if (FLOAT_PATTERN.matcher(stringIn).matches()) {
                return new NBTTagFloat(Float.parseFloat(stringIn.substring(0, stringIn.length() - 1)));
            }

            if (BYTE_PATTERN.matcher(stringIn).matches()) {
                return new NBTTagByte(Byte.parseByte(stringIn.substring(0, stringIn.length() - 1)));
            }

            if (LONG_PATTERN.matcher(stringIn).matches()) {
                return new NBTTagLong(Long.parseLong(stringIn.substring(0, stringIn.length() - 1)));
            }

            if (SHORT_PATTERN.matcher(stringIn).matches()) {
                return new NBTTagShort(Short.parseShort(stringIn.substring(0, stringIn.length() - 1)));
            }

            if (INT_PATTERN.matcher(stringIn).matches()) {
                return new NBTTagInt(Integer.parseInt(stringIn));
            }

            if (DOUBLE_PATTERN.matcher(stringIn).matches()) {
                return new NBTTagDouble(Double.parseDouble(stringIn.substring(0, stringIn.length() - 1)));
            }

            if (DOUBLE_PATTERN_NOSUFFIX.matcher(stringIn).matches()) {
                return new NBTTagDouble(Double.parseDouble(stringIn));
            }

            if ("true".equalsIgnoreCase(stringIn)) {
                return new NBTTagByte((byte) 1);
            }

            if ("false".equalsIgnoreCase(stringIn)) {
                return new NBTTagByte((byte) 0);
            }
        } catch (NumberFormatException ignored) {
        }

        return new NBTTagString(stringIn);
    }

    private String readQuotedString() throws NBTException {
        int i = ++this.cursor;
        StringBuilder sb = null;
        boolean flag = false;

        while (this.canRead()) {
            char currentChar = this.pop();

            if (flag) {
                if (currentChar != '\\' && currentChar != '"') {
                    throw this.exception("Invalid escape of '" + currentChar + "'");
                }

                flag = false;
            } else {
                if (currentChar == '\\') {
                    flag = true;

                    if (sb == null) {
                        sb = new StringBuilder(this.string.substring(i, this.cursor - 1));
                    }

                    continue;
                }

                if (currentChar == '"') {
                    return sb == null ? this.string.substring(i, this.cursor - 1) : sb.toString();
                }
            }

            if (sb != null) {
                sb.append(currentChar);
            }
        }

        throw this.exception("Missing termination quote");
    }

    private String readString() {
        int lastPos = this.cursor;
        while (this.canRead() && this.isAllowedInKey(this.peek())) {
            this.cursor++;
        }

        return this.string.substring(lastPos, this.cursor);
    }

    protected NBTBase readValue() throws NBTException {
        this.skipWhitespace();

        if (!this.canRead()) {
            throw this.exception("Expected value");
        } else {
            char c0 = this.peek();

            if (c0 == '{') {
                return this.readStruct();
            } else {
                return c0 == '[' ? this.readList() : this.readTypedValue();
            }
        }
    }

    protected NBTBase readList() throws NBTException {
        return this.canRead(2) && this.peek(1) != '"' && this.peek(2) == ';' ? this.readArrayTag() : this.readListTag();
    }

    protected NBTTagCompound readStruct() throws NBTException {
        this.expect('{');
        NBTTagCompound tag = new NBTTagCompound();
        this.skipWhitespace();

        while (this.canRead() && this.peek() != '}') {
            String key = this.readKey();

            if (key.isEmpty()) {
                throw this.exception("Expected non-empty key");
            }

            this.expect(':');
            tag.setTag(key, this.readValue());

            if (!this.hasElementSeparator()) {
                break;
            }

            if (!this.canRead()) {
                throw this.exception("Expected key");
            }
        }

        this.expect('}');
        return tag;
    }

    private NBTBase readListTag() throws NBTException {
        this.expect('[');
        this.skipWhitespace();

        if (!this.canRead()) {
            throw this.exception("Expected value");
        } else {
            NBTTagList list = new NBTTagList();
            int i = -1;

            while (this.peek() != ']') {
                NBTBase tag = this.readValue();
                int id = tag.getId();

                if (i < 0) {
                    i = id;
                } else if (id != i) {
                    throw this.exception("Unable to insert " + NBTBase.NBTTypes[id] + " into ListTag of type " + NBTBase.NBTTypes[i]);
                }

                list.appendTag(tag);

                if (!this.hasElementSeparator()) {
                    break;
                }

                if (!this.canRead()) {
                    throw this.exception("Expected value");
                }
            }

            this.expect(']');
            return list;
        }
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    private NBTBase readArrayTag() throws NBTException {
        this.expect('[');
        char currentChar = this.pop();
        this.pop();
        this.skipWhitespace();

        if (!this.canRead()) {
            throw this.exception("Expected value");
        } else if (currentChar == 'B') {
            return new NBTTagByteArray(ArrayUtils.toPrimitive((this.readArray((byte) 7, (byte) 1).toArray(new Byte[0]))));
        } else if (currentChar == 'L') {
            return new NBTTagIntArray(ArrayUtils.toPrimitive(this.readArray((byte) 11, (byte) 3).toArray(new Integer[0])));
        } else if (currentChar == 'I') {
            return new NBTTagIntArray(ArrayUtils.toPrimitive(this.readArray((byte) 11, (byte) 3).toArray(new Integer[0])));
        } else {
            throw this.exception("Invalid array type '" + currentChar + "' found");
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Number> List<T> readArray(byte typeId, byte primitiveType) throws NBTException {
        List<T> list = new ArrayList<>();

        while (true) {
            if (this.peek() != ']') {
                NBTBase tag = this.readValue();
                int i = tag.getId();

                if (i != primitiveType) {
                    throw this.exception("Unable to insert " + NBTBase.NBTTypes[i] + " into " + NBTBase.NBTTypes[typeId]);
                }

                if (primitiveType == 1) {
                    list.add((T) Byte.valueOf(((NBTBase.NBTPrimitive) tag).func_150290_f()));
                } else if (primitiveType == 4) {
                    list.add((T) Long.valueOf(((NBTBase.NBTPrimitive) tag).func_150291_c()));
                } else {
                    list.add((T) Integer.valueOf(((NBTBase.NBTPrimitive) tag).func_150287_d()));
                }

                if (this.hasElementSeparator()) {
                    if (!this.canRead()) {
                        throw this.exception("Expected value");
                    }

                    continue;
                }
            }

            this.expect(']');
            return list;
        }
    }

    private void skipWhitespace() {
        while (this.canRead() && Character.isWhitespace(this.peek())) {
            ++this.cursor;
        }
    }

    private boolean hasElementSeparator() {
        this.skipWhitespace();

        if (this.canRead() && this.peek() == ',') {
            ++this.cursor;
            this.skipWhitespace();
            return true;
        } else {
            return false;
        }
    }

    private void expect(char expected) throws NBTException {
        this.skipWhitespace();
        boolean flag = this.canRead();

        if (flag && this.peek() == expected) {
            ++this.cursor;
        } else {
            throw new Crucible_NBTException("Expected '" + expected + "' but got '" + (flag ? this.peek() : "<EOF>") + "'", this.string, this.cursor + 1);
        }
    }

    protected boolean isAllowedInKey(char charIn) {
        return charIn >= '0' && charIn <= '9' || charIn >= 'A' && charIn <= 'Z' || charIn >= 'a' && charIn <= 'z' || charIn == '_' || charIn == '-' || charIn == '.' || charIn == '+';
    }

    private boolean canRead(int index) {
        return this.cursor + index < this.string.length();
    }

    boolean canRead() {
        return this.canRead(0);
    }

    private char peek(int index) {
        return this.string.charAt(this.cursor + index);
    }

    private char peek() {
        return this.peek(0);
    }

    private char pop() {
        return this.string.charAt(this.cursor++);
    }
}