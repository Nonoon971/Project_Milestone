/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cecs429.text;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tartarus.snowball.*;

/**
 *
 * @author CYBER19
 */
public class TokenProcessorDerived implements TokenProcessor {

    @Override
    public List<String> processToken(String token) {
        List<String> processTokens = new ArrayList<>();

        // if the token has hyphens (-)
        if (token.contains("-")) {
            // Split the token on hyphens 
            for (String term : token.split("-")) {
                //Remove all apostrophes or quotation marks from anywhere in the token.
                String quoteFree = term.replaceAll("[\"']", "");

                //remove all non-alphanumeric characters from the beginning and end of the token, but not the middle.
                //and convert the token to lowercase
                processTokens.add(quoteFree.replaceAll("^[^a-zA-Z0-9]+|[^a-zA-Z0-9]+$", "").toLowerCase());
            }

        } else {
            String quoteFree = token.replaceAll("[\"']", "");
            processTokens.add(quoteFree.replaceAll("^[^a-zA-Z0-9]+|[^a-zA-Z0-9]+$", "").toLowerCase());
        }

        return processTokens;
    }

    @Override
    public String normalizeType(String type) {
        try {
            Class stemClass; 
            
            //PTET FAIRE UN SWITCH POUR GERER DIFFERENTE LANG SI USER CHERCHE EN ANGLAIS ET TT
            
            stemClass = Class.forName("org.tartarus.snowball.ext." + "english" + "Stemmer");
            SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
            stemmer.setCurrent(type);
            stemmer.stem();
            String stemmed_word = stemmer.getCurrent();
            
            return stemmed_word;
            
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(TokenProcessorDerived.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
