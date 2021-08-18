package de.unistuttgart.ma.backend.importer.architecture;

import java.util.List;

import com.shopify.graphql.support.ID;

import de.unistuttgart.gropius.api.ComponentInterfacePageQuery;
import de.unistuttgart.gropius.api.ComponentInterfacePageQueryDefinition;
import de.unistuttgart.gropius.api.ComponentInterfaceQuery;
import de.unistuttgart.gropius.api.ComponentInterfaceQueryDefinition;
import de.unistuttgart.gropius.api.ComponentPageQuery;
import de.unistuttgart.gropius.api.ComponentPageQueryDefinition;
import de.unistuttgart.gropius.api.ComponentQuery;
import de.unistuttgart.gropius.api.ComponentQueryDefinition;
import de.unistuttgart.gropius.api.CreateIssueInput;
import de.unistuttgart.gropius.api.CreateIssuePayloadQuery;
import de.unistuttgart.gropius.api.CreateIssuePayloadQueryDefinition;
import de.unistuttgart.gropius.api.IssueFilter;
import de.unistuttgart.gropius.api.IssuePageQuery;
import de.unistuttgart.gropius.api.IssuePageQueryDefinition;
import de.unistuttgart.gropius.api.IssueQuery;
import de.unistuttgart.gropius.api.IssueQueryDefinition;
import de.unistuttgart.gropius.api.LinkIssueInput;
import de.unistuttgart.gropius.api.LinkIssuePayloadQuery;
import de.unistuttgart.gropius.api.LinkIssuePayloadQueryDefinition;
import de.unistuttgart.gropius.api.MutationQuery;
import de.unistuttgart.gropius.api.MutationQueryDefinition;
import de.unistuttgart.gropius.api.NodeQuery;
import de.unistuttgart.gropius.api.NodeQueryDefinition;
import de.unistuttgart.gropius.api.Operations;
import de.unistuttgart.gropius.api.ProjectFilter;
import de.unistuttgart.gropius.api.ProjectPageQuery;
import de.unistuttgart.gropius.api.ProjectPageQueryDefinition;
import de.unistuttgart.gropius.api.ProjectQuery;
import de.unistuttgart.gropius.api.ProjectQueryDefinition;
import de.unistuttgart.gropius.api.QueryQuery;
import de.unistuttgart.gropius.api.QueryQueryDefinition;
import de.unistuttgart.gropius.api.UserQuery;
import de.unistuttgart.gropius.api.UserQueryDefinition;
import de.unistuttgart.gropius.api.ComponentQuery.IssuesArguments;
import de.unistuttgart.gropius.api.ComponentQuery.IssuesArgumentsDefinition;
import de.unistuttgart.gropius.api.QueryQuery.ProjectsArguments;
import de.unistuttgart.gropius.api.QueryQuery.ProjectsArgumentsDefinition;

/**
 * Provides query queries and mutation queries.
 * 
 * @author maumau
 *
 */
public class GropiusApiQueries {

	private static ProjectPageQueryDefinition projectPageQueryDef;
	
	/**
	 * query for all project with all there components and interface. omit everything else because i dont care. 
	 * @return
	 */
	public static QueryQuery getAllProjectsQuery() {
		QueryQueryDefinition qqd = (QueryQuery qq) -> {qq.projects(projectPageQueryDef);};
		return Operations.query(qqd);
	}
	
	/**
	 * 
	 * @return
	 */
	public static QueryQuery getAllUsersQuery() {
		UserQueryDefinition uqd = (UserQuery uq) -> uq.username();
		
		QueryQueryDefinition qqd = (QueryQuery qq) -> {qq.projects(ppqd -> ppqd.nodes(pqd -> pqd.owner(uqd)));};
		return Operations.query(qqd);
	}
	
	/**
	 * 
	 * @param userName
	 * @return
	 */
	public static QueryQuery getProjectForUserQuery(String userName) {
		QueryQueryDefinition qqd = (QueryQuery qq) -> {qq.projects(makeUserFilter(userName), projectPageQueryDef);};
		return Operations.query(qqd);
	}
	
	/**
	 * 
	 * @param projectName
	 * @return
	 */
	public static QueryQuery getSingleProjectQuery(String projectName) {
		QueryQueryDefinition qqd= (QueryQuery qq) -> {qq.projects(makeFilter(projectName), projectPageQueryDef);};
		return Operations.query(qqd);
	}
	
	static {
		ComponentInterfaceQueryDefinition ciqd = (ComponentInterfaceQuery ciq) -> {ciq.name().component((ComponentQuery cq) -> {cq.name();});};	
		ComponentInterfacePageQueryDefinition cipqd = (ComponentInterfacePageQuery cip) -> {cip.nodes(ciqd);};
		
		ComponentQueryDefinition cqd = (ComponentQuery cq) -> {cq.name().consumedInterfaces(cipqd).interfaces(cipqd);};	
		ComponentPageQueryDefinition cpqd = (ComponentPageQuery cpq) -> {cpq.nodes(cqd);};
	
		ProjectQueryDefinition pqd = (ProjectQuery pq) -> {pq.components(cpqd);};
		projectPageQueryDef = (ProjectPageQuery ppq) -> {ppq.nodes(pqd);};
	}

	private static ProjectsArgumentsDefinition makeUserFilter(String userid) {
		ProjectFilter filter = new ProjectFilter().setOwner(List.of(new ID(userid)));
		
		ProjectsArgumentsDefinition argsDef = (ProjectsArguments args) -> args.filterBy(filter);
		return argsDef;
	}
	
	private static ProjectsArgumentsDefinition makeFilter(String projectName) {
		ProjectFilter filter = new ProjectFilter().setName(projectName);
		
		ProjectsArgumentsDefinition argsDef = (ProjectsArguments args) -> args.filterBy(filter);
		return argsDef;
	}
	
	/**
	 * Create a mutation to create a new issue.
	 * 
	 * @param componentId
	 * @param jsonBody
	 * @param title
	 * @return the mutationQuery to create an issue.
	 */
	public static MutationQuery getCreateIssueMutation(final String componentId, final String jsonBody, final String title) {
		
		// creation request
		CreateIssueInput cii = new CreateIssueInput(title, List.of(new ID(componentId)));
		cii.setBody(jsonBody);
		
		// result
		IssueQueryDefinition iqd = (IssueQuery iq) -> {iq.title();}; 		
		CreateIssuePayloadQueryDefinition queryDef = (CreateIssuePayloadQuery cipq) -> {cipq.issue(iqd);};
		
		// mutation
		MutationQueryDefinition mqd = (MutationQuery mq) -> { mq.createIssue(cii, queryDef);}; 
		
		return Operations.mutation(mqd);
	}
		
	public static MutationQuery getLinkIssueMutation(final ID origin, final ID destination) {
		
		// creation request
		LinkIssueInput lii = new LinkIssueInput(origin, destination);
		
		// result
		LinkIssuePayloadQueryDefinition queryDef = (LinkIssuePayloadQuery lipq) -> {lipq.clientMutationId();};
		
		// mutation
		MutationQueryDefinition mqd = (MutationQuery mq) -> { mq.linkIssue(lii, queryDef);}; 
		
		return Operations.mutation(mqd);
	}
	
	public static QueryQuery getOpenIssueOnComponentQuery(final ID componentId) {
		 
		IssueFilter filterIsOpen = new IssueFilter().setIsOpen(true);
		IssuesArgumentsDefinition issueArgsDef = (IssuesArguments args) -> args.filterBy(filterIsOpen);
		
		IssueQueryDefinition issueQueryDef = (IssueQuery iq) -> {iq.body().title();};
		IssuePageQueryDefinition issuePageQueryDef = (IssuePageQuery ipq) -> {ipq.nodes(issueQueryDef);};
		
		ComponentQueryDefinition compQueryDef = (ComponentQuery cq) ->{cq.issues(issueArgsDef, issuePageQueryDef);}; 
		
		NodeQueryDefinition queryDef = (NodeQuery nq) -> {nq.onComponent(compQueryDef);};
		
		QueryQueryDefinition qqd = (QueryQuery qq) -> {qq.node(componentId, queryDef);};
		
		return Operations.query(qqd);
	}
}
