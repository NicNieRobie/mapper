package dev.vpendischuk.mapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Mapper {
    <T> T readFromString(Class<T> clazz, String input);

    <T> T read(Class<T> clazz, InputStream inputStream) throws IOException;

    <T> T read(Class<T> clazz, File file) throws IOException;

    String writeToString(Object object);

    void write(Object object, OutputStream outputStream) throws IOException;

    void write(Object object, File file) throws IOException;
}
