package dev.vepo.engage.channel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateChannelRequest(@NotBlank(message = "YouTube Channel ID is required") @Size(min = 24, max = 50, message = "YouTube Channel ID must be between 24 and 50 characters") @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "YouTube Channel ID contains invalid characters") String youtubeId) {}