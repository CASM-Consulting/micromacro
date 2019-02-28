package uk.ac.susx.shl.micromacro.core;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import uk.ac.susx.tag.method51.core.gson.GsonBuilderFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Stream;

public class StreamTypeAdapterFactory implements TypeAdapterFactory {

    private static final TypeAdapterFactory streamTypeAdapterFactory = new StreamTypeAdapterFactory();

    private StreamTypeAdapterFactory() {
    }

    static TypeAdapterFactory get() {
        return streamTypeAdapterFactory;
    }

    @Override
    public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> typeToken) {
        // Must be an iterable, subclasses should be picked up by built-in type adapters
        if ( !Stream.class.isAssignableFrom(typeToken.getRawType()) ) {
            // Tell Gson to pick up on its own
            return null;
        }
        // Instantiating a new iterable type adapter
        final TypeAdapter<Stream<Object>> iterableTypeAdapter = new StreamTypeAdapter<>(gson);
        // Cast it cheating javac
        @SuppressWarnings("unchecked")
        final TypeAdapter<T> castTypeAdapter = (TypeAdapter<T>) iterableTypeAdapter;
        return castTypeAdapter;
    }

    private static final class StreamTypeAdapter<E> extends TypeAdapter<Stream<E>> {

        private final Gson gson;

        private StreamTypeAdapter(final Gson gson) {
            this.gson = gson;
        }

        @Override
        @SuppressWarnings("resource")
        public void write(final JsonWriter jsonWriter, final Stream<E> elements)
                throws IOException {
            // Emit [
            jsonWriter.beginArray();
            Iterator<E> itr = elements.iterator();
            while ( itr.hasNext() ) {
                final E e = itr.next();
                gson.toJson(e, e.getClass(), jsonWriter);
            }
            // Emit ]
            jsonWriter.endArray();
        }

        @Override
        @SuppressWarnings("resource")
        public Stream<E> read(final JsonReader jsonReader)
                throws IOException {
            throw new UnsupportedOperationException();
        }


        public static void main(String[] args) {

            System.out.println(GsonBuilderFactory.get().registerTypeAdapterFactory(new StreamTypeAdapterFactory()).create().toJson(ImmutableList.of("blah", "de", "blah").stream()));

        }
    }

}
