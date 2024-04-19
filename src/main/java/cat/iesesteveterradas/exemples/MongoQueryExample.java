package cat.iesesteveterradas.exemples;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.bson.conversions.Bson;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Filters;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MongoQueryExample {
    public static void main(String[] args) {
        try (var mongoClient = MongoClients.create("mongodb://root:example@localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("yourDatabaseName");
            MongoCollection<Document> collection = database.getCollection("yourCollectionName");

            // Calcular la media de ViewCounts
            List<Bson> avgPipeline = Arrays.asList(
                Aggregates.group(null, Accumulators.avg("avgViewCount", "$viewCount"))
            );
            Document avgResult = collection.aggregate(avgPipeline).first();
            double avgViewCount = avgResult.getDouble("avgViewCount");

            // Consulta para obtener preguntas con ViewCount mayor que la media
            Bson query = Filters.gt("viewCount", avgViewCount);
            FindIterable<Document> highViewCountDocuments = collection.find(query);

            // Generar informe 1 en PDF
            generatePDFReport("Informe1", highViewCountDocuments);

            // Consulta para obtener preguntas que contienen en el título cualquiera de las letras específicas
            List<String> letters = Arrays.asList("pug", "wig", "yak", "nap", "jig", "mug", "zap", "gag", "oaf", "elf");
            Bson titleRegexQuery = Filters.regex("title", String.join("|", letters), "i");
            FindIterable<Document> titleContainsLettersDocuments = collection.find(titleRegexQuery);

            // Generar informe 2 en PDF
            generatePDFReport("Informe2", titleContainsLettersDocuments);

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    private static void generatePDFReport(String fileName, FindIterable<Document> documents) throws IOException {
        try (PDDocument pdfDocument = new PDDocument()) {
            PDPage page = new PDPage();
            pdfDocument.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(pdfDocument, page)) {
                contentStream.setLeading(14.5f);

                // Calcular la posición inicial para colocar el texto al principio de la página
                float startY = page.getMediaBox().getHeight() - 20; // Ajusta el valor según necesites

                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 7);
                contentStream.newLineAtOffset(20, startY); // Ajusta el valor de X si deseas un margen lateral

                // Escribir los documentos en el PDF
                for (Document doc : documents) {
                    contentStream.showText(doc.toJson());
                    contentStream.newLine();
                }

                contentStream.endText();
            }

            // Guardar el PDF en la carpeta data/out/ (sobrescribir si ya existe)
            File outputFile = new File("data/out/" + fileName + ".pdf");
            pdfDocument.save(outputFile);
        }
    }
}
