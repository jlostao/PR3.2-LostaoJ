package cat.iesesteveterradas.exemples;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class PR32CreateMain {
    private static final Logger logger = LoggerFactory.getLogger(PR32CreateMain.class);

    public static String cleanHTML(String html) {
        String cleanedText = html.replaceAll("\\<.*?\\>", "").replaceAll("&lt;.*?&gt;", "");
        cleanedText = cleanedText.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").replaceAll("\\s+", " ");
        return cleanedText;
    }

    public static void main(String[] args) {
        try {
            var mongoClient = MongoClients.create("mongodb://root:example@localhost:27017");
            MongoDatabase database = mongoClient.getDatabase("yourDatabaseName");
            MongoCollection<Document> collection = database.getCollection("yourCollectionName");

            File file = new File("data/result.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            org.w3c.dom.NodeList nodeList = doc.getElementsByTagName("row");
            for (int i = 0; i < nodeList.getLength(); i++) {
                org.w3c.dom.Node node = nodeList.item(i);
                if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    org.w3c.dom.Element element = (org.w3c.dom.Element) node;

                    String idString = element.getAttribute("Id");
                    String postTypeIdString = element.getAttribute("PostTypeId");
                    String scoreString = element.getAttribute("Score");
                    String viewCountString = element.getAttribute("ViewCount");
                    String body = element.getAttribute("Body");
                    String ownerUserIdString = element.getAttribute("OwnerUserId");
                    String lastActivityDateString = element.getAttribute("LastActivityDate");
                    String title = element.getAttribute("Title");
                    String tags = element.getAttribute("Tags");
                    String answerCountString = element.getAttribute("AnswerCount");
                    String commentCountString = element.getAttribute("CommentCount");
                    String closedDateString = element.getAttribute("ClosedDate");
                    String contentLicense = element.getAttribute("ContentLicense");

                    int id = idString.isEmpty() ? 0 : Integer.parseInt(idString);
                    int postTypeId = postTypeIdString.isEmpty() ? 0 : Integer.parseInt(postTypeIdString);
                    int score = scoreString.isEmpty() ? 0 : Integer.parseInt(scoreString);
                    int viewCount = viewCountString.isEmpty() ? 0 : Integer.parseInt(viewCountString);
                    int ownerUserId = ownerUserIdString.isEmpty() ? 0 : Integer.parseInt(ownerUserIdString);
                    int answerCount = answerCountString.isEmpty() ? 0 : Integer.parseInt(answerCountString);
                    int commentCount = commentCountString.isEmpty() ? 0 : Integer.parseInt(commentCountString);

                    Document question = new Document("Id", id).append("PostTypeId", postTypeId).append("Score", score).append("ViewCount", viewCount).append("Body", cleanHTML(body)).append("OwnerUserId", ownerUserId).append("LastActivityDate", lastActivityDateString).append("Title", title).append("Tags", tags).append("AnswerCount", answerCount).append("CommentCount", commentCount).append("ClosedDate", closedDateString).append("ContentLicense", contentLicense);

                    // Insertar el documento en la colección MongoDB
                    collection.insertOne(question);

                    // Loggear la inserción del documento
                    logger.info("Document registered in MongoDB: " + question.toJson());
                }
            }

            mongoClient.close();
            logger.info("Document registration was succesful");
        } catch (Exception e) {
            logger.error("Document registration could not be completed: ", e);
        }
    }
}