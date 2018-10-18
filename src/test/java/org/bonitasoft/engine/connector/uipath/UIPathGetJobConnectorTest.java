/**
 * Copyright (C) 2018 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.connector.uipath;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.connector.uipath.model.JobState;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class UIPathGetJobConnectorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @ClassRule
    public static WireMockRule uiPathService = new WireMockRule(8888);

    @Before
    public void configureStubs() throws Exception {
        uiPathService.stubFor(WireMock.post(WireMock.urlEqualTo("/api/account/authenticate"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("mock.authenticate.response.json")));
    }

    private UIPathGetJobConnector createConnector() throws Exception {
        UIPathGetJobConnector uiPathConnector = spy(new UIPathGetJobConnector());
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(UIPathConnector.URL, "http://localhost:8888");
        parameters.put(UIPathConnector.TENANT, "a_tenant");
        parameters.put(UIPathConnector.USER, "admin");
        parameters.put(UIPathConnector.PASSWORD, "somePassowrd");
        parameters.put(UIPathGetJobConnector.JOB_ID, "268348846");
        uiPathConnector.setInputParameters(parameters);
        uiPathConnector.validateInputParameters();
        return uiPathConnector;
    }

    @Test
    public void should_get_pending_job() throws Exception {
        uiPathService.stubFor(WireMock.get(WireMock.urlEqualTo("/odata/Jobs(268348846)"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("mock.job.response.json")));

        UIPathGetJobConnector createConnector = createConnector();
        createConnector.connect();
        Map<String, Object> outputs = createConnector.execute();
        assertThat(outputs.get(UIPathConnector.STATUS_CODE_OUTPUT)).isEqualTo(200);
        assertThat(outputs.get(UIPathGetJobConnector.JOB_STATE)).isEqualTo(JobState.PENDING.toString());
    }

    @Test
    public void should_get_successful_job() throws Exception {
        uiPathService.stubFor(WireMock.get(WireMock.urlEqualTo("/odata/Jobs(268348846)"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("mock.job.success.response.json")));

        UIPathGetJobConnector createConnector = createConnector();
        createConnector.connect();
        Map<String, Object> outputs = createConnector.execute();
        assertThat(outputs.get(UIPathConnector.STATUS_CODE_OUTPUT)).isEqualTo(200);
        assertThat(outputs.get(UIPathGetJobConnector.JOB_STATE)).isEqualTo(JobState.SUCCESSFUL.toString());
        assertThat(outputs.get(UIPathGetJobConnector.JOB_OUTPUT_ARGS)).isEqualTo("{\"out1\" : \"ok\"}");
    }
}