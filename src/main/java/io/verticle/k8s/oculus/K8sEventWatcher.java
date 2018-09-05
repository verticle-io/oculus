package io.verticle.k8s.oculus;

import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import io.verticle.oss.fireboard.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jens Saade
 */

/**
 * A service for watching the k8s api for events and forwarding it to fireboard.
 */
@Service
public class K8sEventWatcher {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    FireboardAccessConfig config;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {


        logger.info("configured token : " + config.getAuthToken());
        logger.info("configured tenant: " + config.getTenantId());
        logger.info("configured bucket: " + config.getBucketId());

        FireboardClient.lazyInit(config);


        KubernetesClient client = new DefaultKubernetesClient();
        logger.info("started k8s watcher " + client.toString());
        client.events().inAnyNamespace().watch(new Watcher<Event>() {

            @Override
            public void eventReceived(Watcher.Action action, Event resource) {
                logger.debug("event " + action.name() + " " + resource.toString());

                StatusEnum status = StatusEnum.info;
                if ("Normal".equalsIgnoreCase(resource.getType())) {
                    status = StatusEnum.success;
                } else if ("Warning".equalsIgnoreCase(resource.getType())) {
                    status = StatusEnum.warn;
                }


                List<MessagePropertySection> sectionList = new ArrayList<>();
                MessagePropertyHelper helper = new MessagePropertyHelper();

                try {


                    helper.section("event")
                            .property("action", resource.getAction())
                            .property("api version", resource.getApiVersion())
                            .property("first timestamp", resource.getFirstTimestamp())
                            .property("last timestamp", resource.getLastTimestamp())
                            .property("kind", resource.getKind())
                            .property("message", resource.getMessage())
                            .property("reason", resource.getReason())
                            .property("reporting component", resource.getReportingComponent())
                            .property("reporting instance", resource.getReportingInstance())
                            .property("type", resource.getType());

                    if (resource.getInvolvedObject() != null) {
                        helper.section("involved object")
                                .property("api version", resource.getInvolvedObject().getApiVersion())
                                .property("field path", resource.getInvolvedObject().getFieldPath())
                                .property("kind", resource.getInvolvedObject().getKind())
                                .property("name", resource.getInvolvedObject().getName())
                                .property("namespace", resource.getInvolvedObject().getNamespace())
                                .property("resource version", resource.getInvolvedObject().getResourceVersion())
                                .property("uid", resource.getInvolvedObject().getUid())
                                .property("foo2", "bar2");
                    }

                    if (resource.getMetadata() != null) {
                        helper.section("metadata")
                                .property("cluster name", resource.getMetadata().getClusterName())
                                .property("generation", resource.getMetadata().getGeneration() != null ? resource.getMetadata().getGeneration().toString() : "")
                                .property("created", resource.getMetadata().getCreationTimestamp())
                                .property("deleted", resource.getMetadata().getDeletionTimestamp())
                                .property("generated name", resource.getMetadata().getGenerateName())
                                .property("name", resource.getMetadata().getName())
                                .property("namespace", resource.getMetadata().getNamespace())
                                .property("resource version", resource.getMetadata().getResourceVersion())
                                .property("self link", resource.getMetadata().getSelfLink())
                                .property("uuid", resource.getMetadata().getUid())
                                .property("annotations", resource.getMetadata().getAnnotations() != null ? resource.getMetadata().getAnnotations().toString() : "")
                                .property("grace period (s)", resource.getMetadata().getDeletionGracePeriodSeconds() != null ? resource.getMetadata().getDeletionGracePeriodSeconds().toString() : "")
                                .property("labels", resource.getMetadata().getLabels() != null ? resource.getMetadata().getLabels().toString() : "")
                                .property("owner references", resource.getMetadata().getOwnerReferences() != null ? resource.getMetadata().getOwnerReferences().toString() : "");
                    }

                    if (resource.getSource() != null) {
                        helper.section("source")
                                .property("component", resource.getSource().getComponent())
                                .property("host", resource.getSource().getHost());
                    }

                } catch (Exception e) {
                    logger.error("error creating metaproperties for fireboard event", e);
                } finally {
                    sectionList = helper.build();
                }


                try {

                    String selfLink = client.getMasterUrl() + resource.getMetadata().getSelfLink();

                    FireboardClient.post(
                            new FireboardMessageBuilder()
                                    .event(resource.getInvolvedObject().getKind() + " " + resource.getInvolvedObject().getName() + " " + resource.getReason())
                                    .category(resource.getMetadata().getNamespace())
                                    .severity(3)
                                    .ident(resource.getSource().getComponent())
                                    .link(new URL(selfLink))
                                    .message(resource.getMessage())
                                    .status(status)
                                    .properties(sectionList)
                                    .build()
                    );

                } catch (Exception e) {
                    logger.error("error sending fireboard event", e);
                }

            }

            @Override
            public void onClose(KubernetesClientException cause) {
                System.out.println("Watcher close due to " + cause);
            }

        });

    }
}

