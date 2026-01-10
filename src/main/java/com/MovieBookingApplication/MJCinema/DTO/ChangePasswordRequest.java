package com.MovieBookingApplication.MJCinema.DTO;


import jakarta.validation.constraints.NotNull;

public class ChangePasswordRequest {

    @NotNull(message = "Old password required.")
    String oldPassword;
    @NotNull(message = "New password required.")
    String newPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
