package de.unistuttgart.ma.backend;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.fail;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.shopify.graphql.support.ID;

import de.unistuttgart.ma.backend.importer.architecture.GropiusApiQueries;
import de.unistuttgart.ma.backend.importer.slo.FlatSolomonRule;
import de.unistuttgart.ma.backend.rest.ImportRequest;

/**
 * Super class for those tests that need a repository, and also make request to
 * other services. The {@code TestWithRepoAndMockServers} provides the
 * repository from {@link TestWithRepo} and also mocks of the solomon and the
 * gropius backends.
 * 
 * 
 * 
 * @author maumau
 *
 */
public abstract class TestWithRepoAndMockServers extends TestWithRepo {

	static WireMockServer server;
	protected static int port;

	protected String base;
	protected String bpmn;

	protected String solomon = "/solomonUrl/";
	protected String gropius = "/gropiusUrl/api";

	protected String solomonEnvironment = "solomonEnvironment";

	protected String issueLocationId = "5e8cf6eaf785a021";

	protected List<FlatSolomonRule> expectedRules;

	ImportRequest request;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	@BeforeEach
	public void setUp() {
		super.setUp();
		try { // Get a free port
			ServerSocket s = new ServerSocket(0);
			port = s.getLocalPort();
			s.close();
		} catch (IOException e) {
			/* No OPS */
		}

		base = "http://localhost:" + port;

		File file = new File("src/test/resources/t2Process.bpmn2");
		try {
			bpmn = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
		} catch (IOException e1) {
			e1.printStackTrace();
			logger.info("reading process from file failed");
		}

		request = new ImportRequest(base + solomon, base + gropius, "t2-extended", solomonEnvironment,
				"ressourceUri.saga", bpmn);

		server = new WireMockServer(port);
		server.start();

		configureFor("localhost", port);

		expectedRules = makeRules();

		mockSolomon();
		mockGropius();
		mockGropiusIssue();
		mockNoIssueGropius();
		mockOpenIssueGropius();
	}

	//////////////////////////////////////////////////////////////////////////////
	// vvv HELPERS TO DO THE MOCKING vvv //
	//////////////////////////////////////////////////////////////////////////////

	/**
	 * prepare mocked reply when querying gropius for projects.
	 */
	public void mockGropius() {
		try {
			String requestUri = gropius + "?query=" + URLEncoder
					.encode(GropiusApiQueries.getSingleProjectQuery(request.getGropiusProjectId()).toString(), "UTF-8");
			stubFor(get(urlEqualTo(requestUri)).willReturn(aResponse().withBody(getProject())));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			fail("Could not setup Test. Mocking Gropius' reply to project queries failed.");
		}
	}

	/**
	 * prepare mocked reply when querying gropius for open issues and there are
	 * some.
	 */
	public void mockOpenIssueGropius() {
		try {
			String requestUri = gropius + "?query=" + URLEncoder.encode(
					GropiusApiQueries.getOpenIssueOnComponentQuery(new ID(issueLocationId)).toString(), "UTF-8");
			stubFor(get(urlEqualTo(requestUri)).willReturn(aResponse().withBody(getOpenIssue())));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			fail("Could not setup Test. Mocking Gropius' reply to queries for open issues failed.");
		}
	}

	/**
	 * prepare mocked reply when querying gropius for projects and there are none.
	 */
	public void mockNoIssueGropius() {
		try {
			String requestUri = gropius + "?query=" + URLEncoder.encode(
					GropiusApiQueries.getOpenIssueOnComponentQuery(new ID(issueLocationId)).toString(), "UTF-8");
			stubFor(get(urlEqualTo(requestUri)).willReturn(aResponse().withBody(getNoOpenIssue())));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			fail("Could not setup Test. Mocking Gropius' 'empty' reply to queries for open issues failed.");
		}
	}

	/**
	 * prepare mocked reply when querying gropius with a mutation to create a new
	 * issue
	 * 
	 * @throws UnsupportedEncodingException
	 */
	public void mockGropiusIssue() {
		String requestUri = gropius;
		stubFor(post(urlEqualTo(requestUri)).willReturn(aResponse().withBody(getIssue())));
	}

	/**
	 * prepare mocked reply for when querying solomon for some slo rules.
	 */
	public void mockSolomon() {
		try {
			String requestUri = solomon + request.getSolomonEnvironment();
			ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
					false);
			stubFor(get(urlEqualTo(requestUri))
					.willReturn(aResponse().withBody(mapper.writeValueAsString(expectedRules))));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			fail("Could not setup Test. Mocking Solomon's reply to queries for slo rules failed.");
		}

	}

	//////////////////////////////////////////////////////////////////////////////
	// vvv HELPERS TO DO VERIFY THE REQUEST TO THE MOCKED SERVERS vvv //
	//////////////////////////////////////////////////////////////////////////////

	/**
	 * verify that someone send {@code calls} numbers of request to the mocked
	 * Gropius server, that queried for the open issues at a component.
	 * 
	 * @param calls expected number of requests
	 * @throws UnsupportedEncodingException
	 */
	public void verifyGetIssueGropius(int calls) throws UnsupportedEncodingException {
		String requestUri = gropius + "?query=" + URLEncoder
				.encode(GropiusApiQueries.getOpenIssueOnComponentQuery(new ID(issueLocationId)).toString(), "UTF-8");
		server.verify(calls, getRequestedFor(urlEqualTo(requestUri)));
	}

	/**
	 * verify that someone send {@code calls} numbers of request to the mocked
	 * Gropius server, that queried for a single project.
	 * 
	 * @param calls expected number of requests
	 * @throws UnsupportedEncodingException
	 */
	public void verifyGetProjectGropius(int calls) throws UnsupportedEncodingException {
		String requestUri = gropius + "?query=" + URLEncoder
				.encode(GropiusApiQueries.getSingleProjectQuery(request.getGropiusProjectId()).toString(), "UTF-8");
		server.verify(calls, getRequestedFor(urlEqualTo(requestUri)));
	}

	/**
	 * verify that someone send {@code calls} numbers of request to the mocked
	 * Gropius server, to create issues. 
	 * 
	 * @param calls expected number of requests
	 */
	public void verifyPostIssueGropius(int calls) {
		String requestUri = gropius;
		server.verify(calls,
				postRequestedFor(urlEqualTo(requestUri)).withHeader("Content-Type", equalTo("application/json")));
	}

	//////////////////////////////////////////////////////////////////////////////
	// vvv HELPERS TO CREATE THE CONTENT RETURNED BY THE MOCKED SERVERS vvv //
	//////////////////////////////////////////////////////////////////////////////

	/**
	 * Creates five {@link FlatSolomonRule}s on different interfaces of the
	 * "t2_base_saga.saga".
	 * 
	 * @return list of five flat rules
	 */
	public List<FlatSolomonRule> makeRules() {
		List<FlatSolomonRule> rules = new ArrayList<>();

		rules.add(makeRules("other_respT_slo", "5e945333924a7004", "5e94539417ca7005"));
		rules.add(makeRules("anotherIface_respT_slo", "5e9453065b4a7002", "5e94553f2a4a7006"));
		rules.add(makeRules("CI_respT_slo", "5e8cf6eaf785a021", "5e8cf780c585a029"));

		rules.add(makeRules("CI_avail_slo", "5e8cf6eaf785a021", "5e8cf780c585a029"));
		rules.add(makeRules("payment_avail_slo", "5e8cf6d4fe05a01f", "5e8cf760d345a028"));

		return rules;
	}

	/**
	 * Create a single {@link FlatSolomonRule}.
	 * 
	 * @param name        used as id, name and description of the rule
	 * @param componentId id of gropius component
	 * @param InterfaceId id of gropius component interface
	 * @return
	 */
	private FlatSolomonRule makeRules(String name, String componentId, String InterfaceId) {
		FlatSolomonRule rule = new FlatSolomonRule(name, name, name, "environment", "alert-id", gropiusId, componentId,
				"preset", "metricOption", "comparisonOption", "statisticsOption", 0.5, 10);
		return rule;
	}

	/**
	 * When you query gropius for the all projects.
	 * 
	 * @return server reply as string
	 */
	private String getProject() {
		return "{\"data\":{\"projects\":{\"nodes\":[{\"id\":\"5e8cc17ed645a00c\", \"name\": \"dummy\",\"components\":{\"nodes\":[{\"id\":\"5e8cf6551a05a013\",\"name\":\"cart\",\"consumedInterfaces\":{\"nodes\":[]},\"interfaces\":{\"nodes\":[{\"id\":\"5e8cf71a0285a023\",\"name\":\"getCart\",\"component\":{\"id\":\"5e8cf6551a05a013\",\"name\":\"cart\"}}]}},{\"id\":\"5e8cf665a445a015\",\"name\":\"uibackend\",\"consumedInterfaces\":{\"nodes\":[{\"id\":\"5e8cf71a0285a023\",\"name\":\"getCart\",\"component\":{\"id\":\"5e8cf6551a05a013\",\"name\":\"cart\"}},{\"id\":\"5e8cf72b87c5a024\",\"name\":\"confirmOrder\",\"component\":{\"id\":\"5e8cf69a1205a01b\",\"name\":\"orchestrator\"}},{\"id\":\"5e8cf7541485a027\",\"name\":\"getInventory\",\"component\":{\"id\":\"5e8cf68f7045a019\",\"name\":\"inventory\"}},{\"id\":\"5e94539417ca7005\",\"name\":\"otherCompIface\",\"component\":{\"id\":\"5e945333924a7004\",\"name\":\"othercomp\"}}]},\"interfaces\":{\"nodes\":[{\"id\":\"5e8cf70605c5a022\",\"name\":\"apiGateway\",\"component\":{\"id\":\"5e8cf665a445a015\",\"name\":\"uibackend\"}}]}},{\"id\":\"5e8cf67ea105a017\",\"name\":\"ui\",\"consumedInterfaces\":{\"nodes\":[{\"id\":\"5e8cf70605c5a022\",\"name\":\"apiGateway\",\"component\":{\"id\":\"5e8cf665a445a015\",\"name\":\"uibackend\"}}]},\"interfaces\":{\"nodes\":[]}},{\"id\":\"5e8cf68f7045a019\",\"name\":\"inventory\",\"consumedInterfaces\":{\"nodes\":[{\"id\":\"5e94553f2a4a7006\",\"name\":\"anotherIface\",\"component\":{\"id\":\"5e9453065b4a7002\",\"name\":\"anothercomp\"}}]},\"interfaces\":{\"nodes\":[{\"id\":\"5e8cf74541c5a026\",\"name\":\"inventoryinterface\",\"component\":{\"id\":\"5e8cf68f7045a019\",\"name\":\"inventory\"}},{\"id\":\"5e8cf7541485a027\",\"name\":\"getInventory\",\"component\":{\"id\":\"5e8cf68f7045a019\",\"name\":\"inventory\"}}]}},{\"id\":\"5e8cf69a1205a01b\",\"name\":\"orchestrator\",\"consumedInterfaces\":{\"nodes\":[{\"id\":\"5e8cf73d3ec5a025\",\"name\":\"orderinterface\",\"component\":{\"id\":\"5e8cf6bf0245a01d\",\"name\":\"order\"}},{\"id\":\"5e8cf74541c5a026\",\"name\":\"inventoryinterface\",\"component\":{\"id\":\"5e8cf68f7045a019\",\"name\":\"inventory\"}},{\"id\":\"5e8cf760d345a028\",\"name\":\"paymentinterface\",\"component\":{\"id\":\"5e8cf6d4fe05a01f\",\"name\":\"payment\"}}]},\"interfaces\":{\"nodes\":[{\"id\":\"5e8cf72b87c5a024\",\"name\":\"confirmOrder\",\"component\":{\"id\":\"5e8cf69a1205a01b\",\"name\":\"orchestrator\"}}]}},{\"id\":\"5e8cf6bf0245a01d\",\"name\":\"order\",\"consumedInterfaces\":{\"nodes\":[]},\"interfaces\":{\"nodes\":[{\"id\":\"5e8cf73d3ec5a025\",\"name\":\"orderinterface\",\"component\":{\"id\":\"5e8cf6bf0245a01d\",\"name\":\"order\"}}]}},{\"id\":\"5e8cf6d4fe05a01f\",\"name\":\"payment\",\"consumedInterfaces\":{\"nodes\":[{\"id\":\"5e8cf780c585a029\",\"name\":\"creditinstituteinterface\",\"component\":{\"id\":\"5e8cf6eaf785a021\",\"name\":\"creditinstitute\"}}]},\"interfaces\":{\"nodes\":[{\"id\":\"5e8cf760d345a028\",\"name\":\"paymentinterface\",\"component\":{\"id\":\"5e8cf6d4fe05a01f\",\"name\":\"payment\"}}]}},{\"id\":\"5e8cf6eaf785a021\",\"name\":\"creditinstitute\",\"consumedInterfaces\":{\"nodes\":[]},\"interfaces\":{\"nodes\":[{\"id\":\"5e8cf780c585a029\",\"name\":\"creditinstituteinterface\",\"component\":{\"id\":\"5e8cf6eaf785a021\",\"name\":\"creditinstitute\"}}]}},{\"id\":\"5e9453065b4a7002\",\"name\":\"anothercomp\",\"consumedInterfaces\":{\"nodes\":[{\"id\":\"5e94539417ca7005\",\"name\":\"otherCompIface\",\"component\":{\"id\":\"5e945333924a7004\",\"name\":\"othercomp\"}}]},\"interfaces\":{\"nodes\":[{\"id\":\"5e94553f2a4a7006\",\"name\":\"anotherIface\",\"component\":{\"id\":\"5e9453065b4a7002\",\"name\":\"anothercomp\"}}]}},{\"id\":\"5e945333924a7004\",\"name\":\"othercomp\",\"consumedInterfaces\":{\"nodes\":[{\"id\":\"5e8cf780c585a029\",\"name\":\"creditinstituteinterface\",\"component\":{\"id\":\"5e8cf6eaf785a021\",\"name\":\"creditinstitute\"}}]},\"interfaces\":{\"nodes\":[{\"id\":\"5e94539417ca7005\",\"name\":\"otherCompIface\",\"component\":{\"id\":\"5e945333924a7004\",\"name\":\"othercomp\"}}]}}]}}]}}}";
	}

	/**
	 * When you query gropius with a mutation to create an issue and the issue
	 * creation succeeded.
	 * 
	 * @return server reply as string
	 */
	private String getIssue() {
		return "{\"data\":{\"createIssue\":{\"issue\":{\"id\":\"5ecbf9b233d6502f\"}}}}";
	}

	/**
	 * When you query gropius for all open issues on the component 5ece9e4fd6ac5003,
	 * and there are open issues.
	 * 
	 * @return server reply as string
	 */
	private String getOpenIssue() {
		return "{\"data\":{\"node\": {\"__typename\": \"Component\",\n" + "\"id\": \"5ece9e4fd6ac5003\",\n"
				+ "      \"issues\": {\n" + "        \"nodes\": ["
				+ "{\"id\": \"5ed60349e7385001\", \"body\": \"[//]: # ({\\\"violatedrule\\\":{\\\"name\\\":\\\"CI_respT_slo\\\",\\\"id\\\":\\\"CI_respT_slo\\\",\\\"threshold\\\":0.0,\\\"period\\\":0.0},\\\"impactlocation\\\":{\\\"name\\\":\\\"payment interface\\\",\\\"id\\\":\\\"Task_4\\\"}})\", \"title\": \"Title\" }"
				+ "]}}}}";
	}

	/**
	 * When you query gropius for all open issues on the component 5ece9e4fd6ac5003,
	 * and there are none.
	 * 
	 * @return server reply as string
	 */
	private String getNoOpenIssue() {
		return "{\"data\":{\n" + "    \"node\": {\n" + "      \"__typename\": \"Component\",\n"
				+ "      \"id\": \"5ece9e4fd6ac5003\",\n" + "      \"issues\": {\n" + "        \"nodes\": []\n"
				+ "      }\n" + "    }\n" + "  }\n" + "}";
	}
}
