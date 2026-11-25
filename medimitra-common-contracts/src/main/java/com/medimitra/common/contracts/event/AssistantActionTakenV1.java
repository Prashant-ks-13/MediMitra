package com.medimitra.common.contracts.event;

import com.medimitra.common.core.UserId;
import java.time.Instant;
import java.util.UUID;

/**
 * Published by AI Assistant on {@code assistant.action_taken} whenever the agent executes a command
 * (e.g. {@code BookAppointmentCommand}) on the user's behalf, for audit and notification.
 */
public record AssistantActionTakenV1(
        UUID actionId, UserId userId, String actionType, String targetId, boolean requiresConfirmation, Instant takenAt) {
}
