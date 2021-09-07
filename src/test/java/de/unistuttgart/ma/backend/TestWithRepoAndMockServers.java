package de.unistuttgart.ma.backend;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
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

public abstract class TestWithRepoAndMockServers extends TestWithRepo {

	static WireMockServer server;
	protected static int port;

	protected String base;
	protected String bpmn;

	protected String solomon = "/solomonUrl/";
	protected String gropius = "/gropiusUrl/api";

	protected String solomonEnvironment = "solomonEnvironment";

	protected String issueLocationId = "5e8cf6eaf785a021";

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
			/* No OPS */ }

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
		stubFor(get(urlEqualTo("/a")).willReturn(aResponse().withBody("A!")));
		stubFor(get(urlEqualTo("/b")).willReturn(aResponse().withBody("B!")));

		try {
			mockSolomon();
			mockGropius();
			mockGropiusIssue();
			mockNoIssueGropius();
			mockOpenIssueGropius();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * prepare mocked reply for gropius project queries
	 * 
	 * @throws UnsupportedEncodingException
	 */
	public void mockGropius() throws UnsupportedEncodingException {

		String requestUri = gropius + "?query=" + URLEncoder
				.encode(GropiusApiQueries.getSingleProjectQuery(request.getGropiusProjectId()).toString(), "UTF-8");
		stubFor(get(urlEqualTo(requestUri)).willReturn(aResponse().withBody(getProject())));
	}

	public void mockOpenIssueGropius() throws UnsupportedEncodingException {
		String requestUri = gropius + "?query=" + URLEncoder
				.encode(GropiusApiQueries.getOpenIssueOnComponentQuery(new ID(issueLocationId)).toString(), "UTF-8");
		stubFor(get(urlEqualTo(requestUri)).willReturn(aResponse().withBody(getOpenIssue())));
	}

	public void mockNoIssueGropius() throws UnsupportedEncodingException {
		String requestUri = gropius + "?query=" + URLEncoder
				.encode(GropiusApiQueries.getOpenIssueOnComponentQuery(new ID(issueLocationId)).toString(), "UTF-8");
		stubFor(get(urlEqualTo(requestUri)).willReturn(aResponse().withBody(getNoOpenIssue())));
	}

	public void verifyGetIssueGropius(int calls) throws UnsupportedEncodingException {
		String requestUri = gropius + "?query=" + URLEncoder
				.encode(GropiusApiQueries.getOpenIssueOnComponentQuery(new ID(issueLocationId)).toString(), "UTF-8");
		server.verify(calls, getRequestedFor(urlEqualTo(requestUri)));
	}

	public void verifyGetProjectGropius(int calls) throws UnsupportedEncodingException {
		String requestUri = gropius + "?query=" + URLEncoder
				.encode(GropiusApiQueries.getSingleProjectQuery(request.getGropiusProjectId()).toString(), "UTF-8");
		server.verify(calls, getRequestedFor(urlEqualTo(requestUri)));
	}

	/**
	 * prepare mocked reply for gropius mutations (?)
	 * 
	 * @throws UnsupportedEncodingException
	 */
	public void mockGropiusIssue() {
		String requestUri = gropius;
		stubFor(post(urlEqualTo(requestUri)).willReturn(aResponse().withBody(getIssue())));
	}

	public void verifyPostIssueGropius(int calls) {
		String requestUri = gropius;
		server.verify(calls,
				postRequestedFor(urlEqualTo(requestUri)).withHeader("Content-Type", equalTo("application/json")));
	}

	/**
	 * prepare mocked reply for solomon
	 * 
	 * @throws JsonProcessingException
	 */
	public void mockSolomon() throws JsonProcessingException {
		String requestUri = solomon + request.getSolomonEnvironment();

		ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		stubFor(get(urlEqualTo(requestUri)).willReturn(aResponse().withBody(mapper.writeValueAsString(getRules()))));
	}

	//
	// HELPERS
	//

	public List<FlatSolomonRule> getRules() {
		List<FlatSolomonRule> rules = new ArrayList<>();

		rules.add(makeRules("other_respT_slo", "5e945333924a7004", "5e94539417ca7005"));
		rules.add(makeRules("anotherIface_respT_slo", "5e9453065b4a7002", "5e94553f2a4a7006"));
		rules.add(makeRules("CI_respT_slo", "5e8cf6eaf785a021", "5e8cf780c585a029"));

		rules.add(makeRules("CI_avail_slo", "5e8cf6eaf785a021", "5e8cf780c585a029"));
		rules.add(makeRules("payment_avail_slo", "5e8cf6d4fe05a01f", "5e8cf760d345a028"));

		return rules;
	}

	private FlatSolomonRule makeRules(String name, String componentId, String InterfaceId) {
		FlatSolomonRule rule = new FlatSolomonRule(name, name, name,"environment", "alert-id", gropiusId, componentId, "preset", "metricOption", "compoarisonOption", "statistisOPtion", 0.5, 10);
		return rule;
	}

	private String getProject() {
		return "{\"data\":{\"projects\":{\"nodes\":[{\"id\":\"5e8cc17ed645a00c\", \"name\": \"dummy\",\"components\":{\"nodes\":[{\"id\":\"5e8cf6551a05a013\",\"name\":\"cart\",\"consumedInterfaces\":{\"nodes\":[]},\"interfaces\":{\"nodes\":[{\"id\":\"5e8cf71a0285a023\",\"name\":\"getCart\",\"component\":{\"id\":\"5e8cf6551a05a013\",\"name\":\"cart\"}}]}},{\"id\":\"5e8cf665a445a015\",\"name\":\"uibackend\",\"consumedInterfaces\":{\"nodes\":[{\"id\":\"5e8cf71a0285a023\",\"name\":\"getCart\",\"component\":{\"id\":\"5e8cf6551a05a013\",\"name\":\"cart\"}},{\"id\":\"5e8cf72b87c5a024\",\"name\":\"confirmOrder\",\"component\":{\"id\":\"5e8cf69a1205a01b\",\"name\":\"orchestrator\"}},{\"id\":\"5e8cf7541485a027\",\"name\":\"getInventory\",\"component\":{\"id\":\"5e8cf68f7045a019\",\"name\":\"inventory\"}},{\"id\":\"5e94539417ca7005\",\"name\":\"otherCompIface\",\"component\":{\"id\":\"5e945333924a7004\",\"name\":\"othercomp\"}}]},\"interfaces\":{\"nodes\":[{\"id\":\"5e8cf70605c5a022\",\"name\":\"apiGateway\",\"component\":{\"id\":\"5e8cf665a445a015\",\"name\":\"uibackend\"}}]}},{\"id\":\"5e8cf67ea105a017\",\"name\":\"ui\",\"consumedInterfaces\":{\"nodes\":[{\"id\":\"5e8cf70605c5a022\",\"name\":\"apiGateway\",\"component\":{\"id\":\"5e8cf665a445a015\",\"name\":\"uibackend\"}}]},\"interfaces\":{\"nodes\":[]}},{\"id\":\"5e8cf68f7045a019\",\"name\":\"inventory\",\"consumedInterfaces\":{\"nodes\":[{\"id\":\"5e94553f2a4a7006\",\"name\":\"anotherIface\",\"component\":{\"id\":\"5e9453065b4a7002\",\"name\":\"anothercomp\"}}]},\"interfaces\":{\"nodes\":[{\"id\":\"5e8cf74541c5a026\",\"name\":\"inventoryinterface\",\"component\":{\"id\":\"5e8cf68f7045a019\",\"name\":\"inventory\"}},{\"id\":\"5e8cf7541485a027\",\"name\":\"getInventory\",\"component\":{\"id\":\"5e8cf68f7045a019\",\"name\":\"inventory\"}}]}},{\"id\":\"5e8cf69a1205a01b\",\"name\":\"orchestrator\",\"consumedInterfaces\":{\"nodes\":[{\"id\":\"5e8cf73d3ec5a025\",\"name\":\"orderinterface\",\"component\":{\"id\":\"5e8cf6bf0245a01d\",\"name\":\"order\"}},{\"id\":\"5e8cf74541c5a026\",\"name\":\"inventoryinterface\",\"component\":{\"id\":\"5e8cf68f7045a019\",\"name\":\"inventory\"}},{\"id\":\"5e8cf760d345a028\",\"name\":\"paymentinterface\",\"component\":{\"id\":\"5e8cf6d4fe05a01f\",\"name\":\"payment\"}}]},\"interfaces\":{\"nodes\":[{\"id\":\"5e8cf72b87c5a024\",\"name\":\"confirmOrder\",\"component\":{\"id\":\"5e8cf69a1205a01b\",\"name\":\"orchestrator\"}}]}},{\"id\":\"5e8cf6bf0245a01d\",\"name\":\"order\",\"consumedInterfaces\":{\"nodes\":[]},\"interfaces\":{\"nodes\":[{\"id\":\"5e8cf73d3ec5a025\",\"name\":\"orderinterface\",\"component\":{\"id\":\"5e8cf6bf0245a01d\",\"name\":\"order\"}}]}},{\"id\":\"5e8cf6d4fe05a01f\",\"name\":\"payment\",\"consumedInterfaces\":{\"nodes\":[{\"id\":\"5e8cf780c585a029\",\"name\":\"creditinstituteinterface\",\"component\":{\"id\":\"5e8cf6eaf785a021\",\"name\":\"creditinstitute\"}}]},\"interfaces\":{\"nodes\":[{\"id\":\"5e8cf760d345a028\",\"name\":\"paymentinterface\",\"component\":{\"id\":\"5e8cf6d4fe05a01f\",\"name\":\"payment\"}}]}},{\"id\":\"5e8cf6eaf785a021\",\"name\":\"creditinstitute\",\"consumedInterfaces\":{\"nodes\":[]},\"interfaces\":{\"nodes\":[{\"id\":\"5e8cf780c585a029\",\"name\":\"creditinstituteinterface\",\"component\":{\"id\":\"5e8cf6eaf785a021\",\"name\":\"creditinstitute\"}}]}},{\"id\":\"5e9453065b4a7002\",\"name\":\"anothercomp\",\"consumedInterfaces\":{\"nodes\":[{\"id\":\"5e94539417ca7005\",\"name\":\"otherCompIface\",\"component\":{\"id\":\"5e945333924a7004\",\"name\":\"othercomp\"}}]},\"interfaces\":{\"nodes\":[{\"id\":\"5e94553f2a4a7006\",\"name\":\"anotherIface\",\"component\":{\"id\":\"5e9453065b4a7002\",\"name\":\"anothercomp\"}}]}},{\"id\":\"5e945333924a7004\",\"name\":\"othercomp\",\"consumedInterfaces\":{\"nodes\":[{\"id\":\"5e8cf780c585a029\",\"name\":\"creditinstituteinterface\",\"component\":{\"id\":\"5e8cf6eaf785a021\",\"name\":\"creditinstitute\"}}]},\"interfaces\":{\"nodes\":[{\"id\":\"5e94539417ca7005\",\"name\":\"otherCompIface\",\"component\":{\"id\":\"5e945333924a7004\",\"name\":\"othercomp\"}}]}}]}}]}}}";
	}

	private String getIssue() {
		return "{\"data\":{\"createIssue\":{\"issue\":{\"id\":\"5ecbf9b233d6502f\"}}}}";
	}

	private String getOpenIssue() {
		return "{\"data\":{\"node\": {\"__typename\": \"Component\",\n"
				+ "\"id\": \"5ece9e4fd6ac5003\",\n" + "      \"issues\": {\n" + "        \"nodes\": ["
						+ "{\"id\": \"5ed60349e7385001\", \"body\": \"[//]: # ({\\\"violatedrule\\\":{\\\"name\\\":\\\"CI_respT_slo\\\",\\\"id\\\":\\\"CI_respT_slo\\\",\\\"threshold\\\":0.0,\\\"period\\\":0.0},\\\"impactlocation\\\":{\\\"name\\\":\\\"payment interface\\\",\\\"id\\\":\\\"Task_4\\\"}})\", \"title\": \"Title\" }"
						+ "]}}}}";
	}

	private String getNoOpenIssue() {
		return "{\"data\":{\n" + "    \"node\": {\n" + "      \"__typename\": \"Component\",\n"
				+ "      \"id\": \"5ece9e4fd6ac5003\",\n" + "      \"issues\": {\n" + "        \"nodes\": []\n"
				+ "      }\n" + "    }\n" + "  }\n" + "}";
	}

}
