// Copyright 2017 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtools.build.lib.buildeventservice;

import static com.google.devtools.build.v1.BuildEvent.BuildComponentStreamFinished.FinishType.FINISHED;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.devtools.build.lib.clock.Clock;
import com.google.devtools.build.v1.BuildEvent;
import com.google.devtools.build.v1.BuildEvent.BuildComponentStreamFinished;
import com.google.devtools.build.v1.BuildEvent.BuildEnqueued;
import com.google.devtools.build.v1.BuildEvent.BuildFinished;
import com.google.devtools.build.v1.BuildEvent.EventCase;
import com.google.devtools.build.v1.BuildEvent.InvocationAttemptFinished;
import com.google.devtools.build.v1.BuildEvent.InvocationAttemptStarted;
import com.google.devtools.build.v1.BuildStatus;
import com.google.devtools.build.v1.BuildStatus.Result;
import com.google.devtools.build.v1.OrderedBuildEvent;
import com.google.devtools.build.v1.PublishBuildToolEventStreamRequest;
import com.google.devtools.build.v1.PublishLifecycleEventRequest;
import com.google.devtools.build.v1.StreamId;
import com.google.devtools.build.v1.StreamId.BuildComponent;
import com.google.protobuf.Any;
import com.google.protobuf.Timestamp;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;

/** Utility class used to build protobuffs requests that are meant to be sent over BES. */
public final class BuildEventServiceProtoUtil {

  private final String buildRequestId;
  private final String buildInvocationId;
  private final String projectId;
  private final AtomicInteger streamSequenceNumber;
  private final String commandName;
  private final Set<String> additionalKeywords;

  public BuildEventServiceProtoUtil(
      String buildRequestId,
      String buildInvocationId,
      @Nullable String projectId,
      String commandName,
      Clock clock,
      Set<String> additionalKeywords) {
    this.buildRequestId = buildRequestId;
    this.buildInvocationId = buildInvocationId;
    this.projectId = projectId;
    this.commandName = commandName;
    this.additionalKeywords = additionalKeywords;
    this.streamSequenceNumber = new AtomicInteger(1);
  }

  public PublishLifecycleEventRequest buildEnqueued(Timestamp timestamp) {
    return lifecycleEvent(
            projectId,
            1,
            com.google.devtools.build.v1.BuildEvent.newBuilder()
                .setEventTime(timestamp)
                .setBuildEnqueued(BuildEnqueued.newBuilder()))
        .build();
  }

  public PublishLifecycleEventRequest buildFinished(Timestamp timestamp, Result result) {
    return lifecycleEvent(
            projectId,
            2,
            com.google.devtools.build.v1.BuildEvent.newBuilder()
                .setEventTime(timestamp)
                .setBuildFinished(
                    BuildFinished.newBuilder()
                        .setStatus(BuildStatus.newBuilder().setResult(result))))
        .build();
  }

  public PublishLifecycleEventRequest invocationStarted(Timestamp timestamp) {
    return lifecycleEvent(
            projectId,
            1,
            com.google.devtools.build.v1.BuildEvent.newBuilder()
                .setEventTime(timestamp)
                .setInvocationAttemptStarted(
                    InvocationAttemptStarted.newBuilder().setAttemptNumber(1)))
        .build();
  }

  public PublishLifecycleEventRequest invocationFinished(Timestamp timestamp, Result result) {
    return lifecycleEvent(
            projectId,
            2,
            com.google.devtools.build.v1.BuildEvent.newBuilder()
                .setEventTime(timestamp)
                .setInvocationAttemptFinished(
                    InvocationAttemptFinished.newBuilder()
                        .setInvocationStatus(BuildStatus.newBuilder().setResult(result))))
        .build();
  }

  public int nextSequenceNumber() {
    return streamSequenceNumber.getAndIncrement();
  }

  /** Creates a PublishBuildToolEventStreamRequest from a packed bazel event. */
  public PublishBuildToolEventStreamRequest bazelEvent(
      int sequenceNumber, Timestamp timestamp, Any packedEvent) {
    return publishBuildToolEventStreamRequest(
        sequenceNumber,
        timestamp,
        com.google.devtools.build.v1.BuildEvent.newBuilder().setBazelEvent(packedEvent));
  }

  public PublishBuildToolEventStreamRequest streamFinished(
      int sequenceNumber, Timestamp timestamp) {
    return publishBuildToolEventStreamRequest(
        sequenceNumber,
        timestamp,
        BuildEvent.newBuilder()
            .setComponentStreamFinished(
                BuildComponentStreamFinished.newBuilder().setType(FINISHED)));
  }

  @VisibleForTesting
  public PublishBuildToolEventStreamRequest publishBuildToolEventStreamRequest(
      int sequenceNumber, Timestamp timestamp, BuildEvent.Builder besEvent) {
    PublishBuildToolEventStreamRequest.Builder builder =
        PublishBuildToolEventStreamRequest.newBuilder()
            .setOrderedBuildEvent(
                OrderedBuildEvent.newBuilder()
                    .setSequenceNumber(sequenceNumber)
                    .setEvent(besEvent.setEventTime(timestamp))
                    .setStreamId(streamId(besEvent.getEventCase())));
    if (sequenceNumber == 1) {
      builder.addAllNotificationKeywords(getKeywords());
    }
    return builder.build();
  }

  @VisibleForTesting
  public PublishLifecycleEventRequest.Builder lifecycleEvent(@Nullable String projectId,
      int sequenceNumber, BuildEvent.Builder lifecycleEvent) {
    PublishLifecycleEventRequest.Builder builder = PublishLifecycleEventRequest.newBuilder()
        .setServiceLevel(PublishLifecycleEventRequest.ServiceLevel.INTERACTIVE)
        .setBuildEvent(
            OrderedBuildEvent.newBuilder()
                .setSequenceNumber(sequenceNumber)
                .setStreamId(streamId(lifecycleEvent.getEventCase()))
                .setEvent(lifecycleEvent));
    if (projectId != null) {
      builder.setProjectId(projectId);
    }
    return builder;
  }

  @VisibleForTesting
  public StreamId streamId(EventCase eventCase) {
    StreamId.Builder streamId = StreamId.newBuilder().setBuildId(buildRequestId);
    switch (eventCase) {
      case BUILD_ENQUEUED:
      case BUILD_FINISHED:
        streamId.setComponent(BuildComponent.CONTROLLER);
        break;
      case INVOCATION_ATTEMPT_STARTED:
      case INVOCATION_ATTEMPT_FINISHED:
        streamId.setInvocationId(buildInvocationId);
        streamId.setComponent(BuildComponent.CONTROLLER);
        break;
      case BAZEL_EVENT:
      case COMPONENT_STREAM_FINISHED:
        streamId.setInvocationId(buildInvocationId);
        streamId.setComponent(BuildComponent.TOOL);
        break;
      default:
        throw new IllegalArgumentException("Illegal EventCase " + eventCase);
    }
    return streamId.build();
  }

  /** Keywords used by BES subscribers to filter notifications */
  private ImmutableSet<String> getKeywords() {
    return ImmutableSet.<String>builder()
        .add("command_name=" + commandName)
        .add("protocol_name=BEP")
        .addAll(additionalKeywords)
        .build();
  }
}
