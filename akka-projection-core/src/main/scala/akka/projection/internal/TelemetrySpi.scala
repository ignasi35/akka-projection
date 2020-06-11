/*
 * Copyright (C) 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.projection.internal

import akka.actor.ActorSystem
import akka.projection.ProjectionId

object TelemetryProvider {
  def start(projectionId: ProjectionId)(system: ActorSystem): Telemetry = ???
}

class Telemetry {

  // Per projection
  def failed(projectionId: ProjectionId, cause: Throwable): Unit = { /* will release all internals*/ }
  def stopped(projectionId: ProjectionId): Unit = { /* will release all internals*/ }

  // Per envelope
  def beforeProcess[Offset, Envelope](projectionId: ProjectionId): AnyRef = { ??? }
  def afterProcess[Offset, Envelope](projectionId: ProjectionId, telemetryContext: AnyRef): Unit = {}
  // Only invoked when the offset is committed. Pass the number of envelopes committed
  def onEnvelopeSuccess[Offset, Envelope](projectionId: ProjectionId, successCount: Int): Unit = {}
  // Invoked when processing an envelope fails. If the operation is part of a batch
  // or a group it will be invoked once anyway
  def error[Offset, Envelope](projectionId: ProjectionId, cause: Throwable): Unit = {}
}