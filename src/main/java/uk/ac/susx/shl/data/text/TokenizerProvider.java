package uk.ac.susx.shl.data.text;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by sw206 on 15/05/2018.
 */
public class TokenizerProvider {

    private static Tokenizer tokenizer;

    public synchronized static Tokenizer get() {
        if(tokenizer ==  null) {
            try {
                TokenizerModel tokenModel = new TokenizerModel(Files.newInputStream(Paths.get("en-token.bin")));
                tokenizer = new TokenizerME(tokenModel);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return tokenizer;
    }
}
