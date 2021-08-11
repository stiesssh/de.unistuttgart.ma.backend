package throwaway;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import de.unistuttgart.gropius.api.ComponentInterface;
import de.unistuttgart.ma.backend.TestWithRepo;
import de.unistuttgart.ma.backend.repository.NotificationRepository;
import de.unistuttgart.ma.impact.Impact;
import de.unistuttgart.ma.impact.ImpactFactory;
import de.unistuttgart.ma.impact.Notification;
import de.unistuttgart.ma.impact.Violation;

public class NotificationRepoTest extends TestWithRepo {

	@Autowired NotificationRepository repo;
	
	/** 
	 * I guess the problem is, that the impact chain is linked to the system and serializing the entire system is just tooo much.
	 * 
	 * If i want to solve this i must either go back to serializing with ecore and savogn xml strings,
	 * or build a separate db item. 
	 * 
	 * @throws IOException
	 */
	@Test
	public void test() throws IOException {
//		loadSystem();
//		Notification note = createImpactChain();
//		
//		
//		repo.save(note);
//		
		
	}
	
}
