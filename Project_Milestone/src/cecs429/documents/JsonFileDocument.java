/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cecs429.documents;

import java.io.*;
import java.nio.file.Path;
//json-simple-2.1.2 jar
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

/**
 *
 * @author CYBER19
 */
public class JsonFileDocument implements FileDocument {

    private int mDocumentId;
    private Path mFilePath;

    public JsonFileDocument(int documentId, Path filePath) {
        mDocumentId = documentId;
        mFilePath = filePath;
    }

    @Override
    public Path getFilePath() {
        return mFilePath;
    }

    @Override
    public int getId() {
        return mDocumentId;
    }

    @Override
    //From https://waytolearnx.com/2020/03/lire-un-fichier-json-avec-java.html#google_vignette website
    public Reader getContent() {
        JSONParser jsonP = new JSONParser();

        try {            
            // Parsing JSON file 
            JSONObject ob = (JSONObject) jsonP.parse(new FileReader(mFilePath.toString()));

            // Get back the json "body" content
            String content = (String) ob.get("body");

            return new StringReader(content);
        } catch (ParseException | IOException e) {
            // Gestion des exceptions en cas d'erreur de parsing ou de lecture de fichier
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getTitle() {
        JSONParser jsonP = new JSONParser();

        try {
            // Parsing JSON file 
            JSONObject ob = (JSONObject) jsonP.parse(new FileReader(mFilePath.toString()));

            // Récupération de la propriété "title" du JSON
            String title = (String) ob.get("title");

            return title;
        } catch (ParseException | IOException e) {
            // Gestion des exceptions en cas d'erreur de parsing ou de lecture de fichier
            throw new RuntimeException(e);
        }
    }

    public static FileDocument loadJsonFileDocument(Path absoluteFilePath, int documentId) {
        return new JsonFileDocument(documentId, absoluteFilePath);
    }
}
