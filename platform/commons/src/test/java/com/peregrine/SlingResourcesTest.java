package com.peregrine;

import com.peregrine.commons.util.PerConstants;
import com.peregrine.mock.PageMock;
import com.peregrine.mock.ResourceMock;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.util.Arrays;
import java.util.List;

import static com.peregrine.commons.util.PerConstants.APPS_ROOT;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SlingResourcesTest {

    public static final String RESOURCE_TYPE = "per/component";
    public static final String SLASH_APPS_SLASH = APPS_ROOT + SLASH;

    protected static final String NN_ROOT = PerConstants.NN_CONTENT;
    protected static final String NN_PARENT = "parent";
    protected static final String NN_PAGE = "page";
    protected static final String NN_RESOURCE = "resource";
    protected static final String PAGE_PATH = SLASH + NN_ROOT + SLASH + NN_PARENT + SLASH + NN_PAGE;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected final ResourceMock repoRoot = new ResourceMock("Repository Root");
    protected final ResourceMock root = new ResourceMock("Root");
    protected final ResourceMock parent = new ResourceMock("Parent");
    protected final PageMock page = new PageMock("Page");
    protected final ResourceMock content = page.getContent();
    protected final ResourceMock resource = new ResourceMock("Resource");

    protected final List<ResourceMock> resources = Arrays.asList(root, parent, page, content, resource);

    protected final ResourceResolverFactory resolverFactory = mock(ResourceResolverFactory.class, fullName("Resolver Factory"));
    protected final ResourceResolver resourceResolver = mock(ResourceResolver.class, fullName("Resource Resolver"));
    protected final Session session = mock(Session.class, fullName("Session"));

    protected final PageMock component = new PageMock("Per Component");

    protected final SlingHttpServletRequest request = mock(SlingHttpServletRequest.class, fullName("Request"));

    public SlingResourcesTest() {
        setPaths();
        setParentChildRelationships();
        initResources();
        component.setPath(SLASH_APPS_SLASH + RESOURCE_TYPE);
        init(component);
        bindResolverFactory();
        bindRequest();
    }

    private void setPaths() {
        repoRoot.setPath(SLASH);
        setPaths(PAGE_PATH, root, parent, page);
        resource.setPath(content.getPath() + SLASH + NN_RESOURCE);
    }

    protected static void setPaths(final String path, final ResourceMock... resources) {
        String currentPath = path;
        for (int i = resources.length - 1; i >= 0; i--) {
            resources[i].setPath(currentPath);
            currentPath = StringUtils.substringBeforeLast(currentPath, SLASH);
        }
    }

    private void setParentChildRelationships() {
        setParentChildRelationships(repoRoot, root, parent, page);
        setParentChildRelationships(content, resource);
    }

    protected static void setParentChildRelationships(final ResourceMock... resources) {
        for (int i = 0; i < resources.length - 1; i++) {
            final ResourceMock parent = resources[i];
            final ResourceMock child = resources[i + 1];
            child.setParent(parent);
            parent.addChild(child);
        }
    }

    private void initResources() {
        init(repoRoot);
        for (final ResourceMock mock: resources) {
            init(mock);
        }
    }

    private void bindResolverFactory() {
        try {
            when(resolverFactory.getServiceResourceResolver(Mockito.any())).thenReturn(resourceResolver);
            when(resolverFactory.getResourceResolver(Mockito.any())).thenReturn(resourceResolver);
            when(resolverFactory.getThreadResourceResolver()).thenReturn(resourceResolver);
        } catch (final LoginException e) {
        }
    }

    private void bindRequest() {
        when(request.getResource()).thenReturn(resource);
        when(request.getResourceResolver()).thenReturn(resourceResolver);
    }

    public Logger getLogger() {
        return logger;
    }

    protected <Mock extends ResourceMock> Mock init(final Mock mock) {
        mock.setResourceResolver(resourceResolver);
        mock.setSession(session);
        return mock;
    }

    private String fullName(final String name) {
        return SlingResourcesTest.class.getSimpleName() + " " + name;
    }
}
