package net.cubespace.Yamler.Config;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class InvalidConverterException extends Exception {
    public InvalidConverterException() {}

    public InvalidConverterException(String msg) {
        super(msg);
    }

    public InvalidConverterException(Throwable cause) {
        super(cause);
    }

    public InvalidConverterException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
