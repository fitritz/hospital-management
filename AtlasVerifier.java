import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.time.Instant;

public class AtlasVerifier {
  public static void main(String[] args) {
    String uri = System.getProperty("mongo.uri");
    if (uri == null || uri.isEmpty()) {
      uri = System.getenv("MONGO_URI");
    }
    if (uri == null || uri.isEmpty()) {
      System.err.println("Missing mongo.uri system property or MONGO_URI environment variable");
      System.exit(1);
    }

    try (MongoClient client = MongoClients.create(uri)) {
      MongoDatabase db = client.getDatabase("hospital_management");
      MongoCollection<Document> coll = db.getCollection("patients");
      String marker = "TEST_FROM_AGENT_" + Instant.now().toEpochMilli();
      Document doc = new Document("name", marker)
          .append("age", 99)
          .append("gender", "Other")
          .append("contact", "agent-test")
          .append("createdAt", Instant.now().toString());
      coll.insertOne(doc);
      System.out.println("Inserted test patient with marker: " + marker);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(2);
    }
  }
}
