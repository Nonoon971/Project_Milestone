package cecs429.text;

import java.util.*;

/**
 * A BasicTokenProcessor creates terms from tokens by removing all
 * non-alphanumeric characters from the token, and converting it to all
 * lowercase.
 */
public class BasicTokenProcessor implements TokenProcessor {

    

    @Override
    public List<String> processToken(String token) {
        List<String> processTokens = new ArrayList<>();
        
        processTokens.add(token.replaceAll("\\W", "").toLowerCase());

        return processTokens;
    }

    @Override
    public String normalizeType(String type) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
