package com.sensorberg.sdk.internal.http;

import android.util.Log;

import com.android.sensorbergVolley.Network;
import com.android.sensorbergVolley.VolleyError;
import com.sensorberg.sdk.SensorbergApplicationTest;
import com.sensorberg.sdk.model.BeaconId;
import com.sensorberg.sdk.internal.BeaconResponseHandler;
import com.sensorberg.sdk.internal.OkHttpClientTransport;
import com.sensorberg.sdk.internal.Transport;
import com.sensorberg.sdk.internal.transport.HeadersJsonObjectRequest;
import com.sensorberg.sdk.internal.transport.SettingsCallback;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.resolver.ResolutionConfiguration;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.ScanEventType;
import com.sensorberg.sdk.settings.Settings;
import com.sensorberg.sdk.testUtils.TestPlatform;

import org.fest.assertions.api.Assertions;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import util.TestConstants;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class TransportShould extends SensorbergApplicationTest {

    private static final UUID BEACON_ID = UUID.fromString("192E463C-9B8E-4590-A23F-D32007299EF5");
    private static final int MAJOR = 1337;
    private static final int MINOR = 1337;

    protected Transport tested;
    protected TestPlatform testPlattform;
    private ScanEvent scanEvent;
    private Settings settings;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        testPlattform = spy(new TestPlatform().setContext(getApplication()));

        scanEvent = new ScanEvent.Builder()
                .withBeaconId(new BeaconId(BEACON_ID, MAJOR, MINOR))
                .withEventMask(ScanEventType.ENTRY.getMask())
                .withEventTime(testPlattform.getClock().now())
                .build();

        settings = mock(Settings.class);

        tested = new OkHttpClientTransport(testPlattform, settings);
        tested.setApiToken(TestConstants.API_TOKEN);

    }

    public void test_should_forward_the_layout_opload_interval_to_the_settings() throws Exception {
        startWebserver(com.sensorberg.sdk.test.R.raw.resolve_resolve_with_report_trigger);
        tested.getBeacon(new ResolutionConfiguration.Builder()
                .withScanEvent(scanEvent)
                .build(), BeaconResponseHandler.NONE);

        waitForRequests(1);
        verify(settings).historyUploadIntervalChanged(1337L * 1000);
    }

    public void test_failures() throws VolleyError {
        Network network = testPlattform.getSpyNetwork();
        doThrow(new VolleyError()).when(network).performRequest(any(HeadersJsonObjectRequest.class));

        tested.getSettings(new SettingsCallback() {
            @Override
            public void nothingChanged() {
                fail();
            }

            @Override
            public void onFailure(Throwable e) {
                Log.d("FOO", "onFailure" + e.getLocalizedMessage());
            }

            @Override
            public void onSettingsFound(JSONObject settings) {
                fail();
            }
        });
    }

    // https://manage.sensorberg.com/#/campaign/edit/0ec64004-18a5-41df-a5dc-810d395dec83
    public void test_a_beacon_request(){
        tested.getBeacon(new ResolutionConfiguration(scanEvent), new BeaconResponseHandler() {
            @Override
            public void onSuccess(List<BeaconEvent> foundBeaconEvents) {
                Assertions.assertThat(foundBeaconEvents).isNotNull().hasSize(1);
            }

            @Override
            public void onFailure(Throwable cause) {
                fail("there was a failure with this request");
            }
        });
    }

    // https://manage.sensorberg.com/#/campaign/edit/0ec64004-18a5-41df-a5dc-810d395dec83
    public void test_should_be_synchronous(){
        final CountDownLatch latch = new CountDownLatch(1);
        tested.getBeacon(new ResolutionConfiguration(scanEvent), new BeaconResponseHandler() {
            @Override
            public void onSuccess(List<BeaconEvent> foundBeaconEvents) {
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable cause) {
                latch.countDown();
            }
        });
        Assertions.assertThat(latch.getCount()).isEqualTo(0);
    }

    public void test_a_settings_request(){
        tested.getSettings(new SettingsCallback() {
            @Override
            public void nothingChanged() {
                fail("there should be changes to no settings");
            }

            @Override
            public void onFailure(Throwable e) {
                fail("there was a failure with this request");
            }

            @Override
            public void onSettingsFound(JSONObject settings) {
                Assertions.assertThat(settings).isNotNull();
            }
        });
    }
}