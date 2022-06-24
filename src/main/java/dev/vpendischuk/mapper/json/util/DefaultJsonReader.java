package dev.vpendischuk.mapper.json.util;

import dev.vpendischuk.mapper.json.exceptions.JsonReadException;
import dev.vpendischuk.mapper.json.types.JsonArray;
import dev.vpendischuk.mapper.json.types.JsonObject;
import dev.vpendischuk.mapper.json.types.JsonString;
import dev.vpendischuk.mapper.json.types.JsonValue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * The default implementation of the {@link JsonReader} interface
 *   that is used by the {@link dev.vpendischuk.mapper.json.JsonMapper}
 *   to read JSON data from input streams and strings.
 * <p>
 * Initialization examples:
 * <pre>
 * String data = "{\"name\":\"Jason\"}";
 * // From string, retains identity
 * JsonReader reader = new DefaultJsonReader(data, true);
 *
 * // From file, does not retain identity
 * JsonReader reader = new DefaultJsonReader(new FileReader("/dir/file"), false);
 * </pre>
 */
public class DefaultJsonReader implements JsonReader {
    // Reference resolver used by JSON reader to generate
    //   JSON values while retaining identity.
    private final JsonMapReferenceResolver mapReferenceResolver;
    // Reader used to get data.
    private final Reader reader;
    // Flag that denotes if reference equality is maintained.
    private final boolean retainIdentity;
    // Flag that denotes if the end of file was reached by the reader.
    private boolean eofReached;
    // Flag that denotes if the previous character has to be returned next.
    private boolean steppedBack;
    // Flag that denotes the current position of read on the line.
    private long currentCharPos;
    // Flag that denotes the current position of read in the stream.
    private long currentReadIndex;
    // Amount of characters read on previous line.
    private long readOnPrevLine;
    // Previously read character.
    private char previousChar;

    /**
     * Initializes a new {@link DefaultJsonReader} instance with specified parameters.
     *
     * @param reader reader used to obtain data from the input stream.
     * @param retainIdentity flag that denotes if reference equality is maintained.
     */
    public DefaultJsonReader(Reader reader, boolean retainIdentity) {
        this.reader = reader;
        eofReached = false;
        steppedBack = false;
        currentCharPos = 1;
        currentReadIndex = 0;
        readOnPrevLine = 0;
        previousChar = 0;
        this.retainIdentity = retainIdentity;
        mapReferenceResolver = new DefaultJsonMapReferenceResolver();
    }

    /**
     * Initializes a new {@link DefaultJsonReader} instance with specified parameters.
     *
     * @param string string which stores the JSON data that is to be read.
     * @param retainIdentity flag that denotes if reference equality is maintained.
     */
    public DefaultJsonReader(String string, boolean retainIdentity) {
        this(new StringReader(string), retainIdentity);
    }

    /**
     * Obtains the next character in the JSON, disregarding the whitespaces.
     *
     * @return the next character in the JSON, disregarding the whitespaces.
     */
    @Override
    public char nextCharacterTrimmed() {
        for (;;) {
            char readChar = nextCharacter();
            if (readChar == 0 || readChar > ' ') {
                return readChar;
            }
        }
    }

    /**
     * Obtains the next character in the JSON.
     *
     * @return the next character in the JSON.
     */
    private char nextCharacter() throws JsonReadException {
        int readChar;
        if (steppedBack) {
            readChar = previousChar;
            steppedBack = false;
        } else {
            try {
                readChar = reader.read();
            } catch (IOException ex) {
                throw new JsonReadException("[JSON Reader Error] Could not read from source.", ex);
            }
        }

        if (readChar <= 0) {
            eofReached = true;
            return 0;
        }

        movePositionForward(readChar);
        previousChar = (char) readChar;
        return previousChar;
    }

    /**
     * Moves the read position forward in the stream.
     *
     * @param readChar code of the character that was read before the move.
     */
    private void movePositionForward(int readChar) {
        if (readChar > 0) {
            currentReadIndex++;
            if (readChar == '\r') {
                readOnPrevLine = currentCharPos;
                currentCharPos = 0;
            } else if (readChar == '\n') {
                if (previousChar != '\r') {
                    readOnPrevLine = currentCharPos;
                }
                currentCharPos = 0;
            } else {
                currentCharPos++;
            }
        }
    }

    /**
     * Moves the read position backwards in the stream.
     */
    private void movePositionBackward() {
        currentReadIndex--;
        if (previousChar == '\r' || previousChar == '\n') {
            currentCharPos = readOnPrevLine;
        } else if (currentCharPos > 0) {
            currentCharPos--;
        }
    }

    /**
     * Returns the next value parsed from a JSON.
     *
     * @return {@link JsonValue} parsed from the next text value in the JSON.
     */
    @Override
    public JsonValue nextValue() throws JsonReadException {
        char readChar = nextCharacterTrimmed();
        String valueString;

        switch (readChar) {
            case '\'':
            case '\"':
                return nextString(readChar);
            case '{':
                moveBack();
                return new JsonObject(this);
            case '[':
                moveBack();
                return new JsonArray(this);
        }

        StringBuilder stringBuilder = new StringBuilder();
        while (readChar >= ' ' && ",:]}/\\\"[{;=#".indexOf(readChar) < 0) {
            stringBuilder.append(readChar);
            readChar = nextCharacter();
        }

        if (!eofReached) {
            moveBack();
        }

        valueString = stringBuilder.toString();

        if (valueString.isEmpty()) {
            throw new JsonReadException("[JSON Reader Error] Syntax error: missing value.");
        }
        return JsonValue.stringToPrimitiveJsonValue(valueString);
    }

    /**
     * Returns the next string in the JSON data (relative to the current read position).
     *
     * @param quote character used to denote a quote - limits of the string value.
     * @return the next string in the JSON data.
     * @throws JsonReadException if a syntax error was detected.
     */
    private JsonString nextString(char quote) throws JsonReadException {
        char readChar;

        StringBuilder stringBuilder = new StringBuilder();

        for (;;) {
            readChar = nextCharacter();
            switch (readChar) {
                case '\n', '\r' -> throw new JsonReadException("[JSON Reader Error] Syntax error: no string terminator.");
                case '\\' -> {
                    // Writing escape sequences to string.
                    readChar = nextCharacter();
                    switch (readChar) {
                        case 'n' -> stringBuilder.append('\n');
                        case 'r' -> stringBuilder.append('\r');
                        case 'f' -> stringBuilder.append('\f');
                        case 'b' -> stringBuilder.append('\b');
                        case 't' -> stringBuilder.append('\t');
                        case '"', '\'', '\\', '/' -> stringBuilder.append(readChar);
                        default -> throw new JsonReadException("[JSON Reader Error] Syntax error: illegal escape.");
                    }
                }
                default -> {
                    if (readChar == quote) {
                        // On string end.
                        return new JsonString(stringBuilder.toString());
                    }

                    // Appending string's characters.
                    stringBuilder.append(readChar);
                }
            }
        }
    }

    /**
     * Returns the character that was previously read from JSON.
     *
     * @return the previously read character.
     */
    @Override
    public char previousCharacter() {
        return previousChar;
    }

    /**
     * Moves a character back in the JSON reading process.
     *
     * @throws JsonReadException if the method was used twice without moving forward once -
     *                           stepping back can be used only for a single character.
     */
    @Override
    public void moveBack() throws JsonReadException {
        if (steppedBack || currentReadIndex <= 0) {
            throw new JsonReadException("[JSON Reader Error] Unable to move back.");
        }

        movePositionBackward();
        steppedBack = true;
        eofReached = false;
    }

    /**
     * Returns the reference resolver registered for used by the reader.
     *
     * @return the reference resolver.
     */
    @Override
    public JsonMapReferenceResolver getReferenceResolver() {
        return mapReferenceResolver;
    }

    /**
     * Returns the reader's identity retention flag.
     *
     * @return the reader's identity retention {@code boolean} flag.
     */
    @Override
    public boolean getIdentityRetainFlag() {
        return retainIdentity;
    }
}
