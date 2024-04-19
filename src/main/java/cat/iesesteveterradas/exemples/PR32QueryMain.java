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

public class PR32QueryMain {
    public static void main(String[] args) {
        try (var mongoClient = MongoClients.create("mongodb://root:example@localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("yourDatabaseName");
            MongoCollection<Document> collection = database.getCollection("yourCollectionName");

            List<Bson> avgPipeline = Arrays.asList(
                Aggregates.group(null, Accumulators.avg("avgViewCount", "$viewCount"))
            );
            Document avgResult = collection.aggregate(avgPipeline).first();
            double avgViewCount = avgResult.getDouble("avgViewCount");

            Bson query = Filters.gt("viewCount", avgViewCount);
            FindIterable<Document> highViewDocuments = collection.find(query);

            informeGenerator("informe1", highViewDocuments);

            List<String> letters = Arrays.asList("pug", "wig", "yak", "nap", "jig", "mug", "zap", "gag", "oaf", "elf");
            Bson titleRegexQuery = Filters.regex("title", String.join("|", letters), "i");
            FindIterable<Document> documentTitlesStartWith = collection.find(titleRegexQuery);

            informeGenerator("informe2", documentTitlesStartWith);

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    private static void informeGenerator(String fileName, FindIterable<Document> documents) throws IOException {
        try (PDDocument pdfDocument = new PDDocument()) {
            PDPage page = new PDPage();
            pdfDocument.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(pdfDocument, page)) {
                contentStream.setLeading(14.5f);

                float startY = page.getMediaBox().getHeight() - 20; 

                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 5);
                contentStream.newLineAtOffset(20, startY); 

                for (Document doc : documents) {
                    contentStream.showText(doc.toJson());
                    contentStream.newLine();
                }

                contentStream.endText();
            }

            File outputFile = new File("data/out/" + fileName + ".pdf");
            pdfDocument.save(outputFile);
        }
    }
}
