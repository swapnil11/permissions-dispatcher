package permissions.dispatcher;

import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.View;

import java.io.Serializable;

public class PermissionMessage implements Serializable {
    @StringRes
    private final int messageResId;
    @StringRes
    private final int actionResId;
    private final View.OnClickListener listener;
    private final int duration;

    private PermissionMessage(Builder builder) {
        this.messageResId = builder.messageResId;
        this.actionResId = builder.actionResId;
        this.listener = builder.listener;
        this.duration = builder.duration;
    }

    @StringRes
    public int getMessageResId() {
        return messageResId;
    }

    @StringRes
    public int getActionResId() {
        return actionResId;
    }

    public View.OnClickListener getListener() {
        return listener;
    }

    public int getDuration() {
        return duration;
    }

    public static class Builder {
        @StringRes
        private int messageResId;
        @StringRes
        private int actionResId;
        private View.OnClickListener listener;
        private int duration = Snackbar.LENGTH_LONG;

        public Builder() {
        }

        public Builder messageResId(@StringRes int val) {
            this.messageResId = val;
            return this;
        }

        public Builder actionResId(@StringRes int val) {
            this.actionResId = val;
            return this;
        }

        public Builder listener(View.OnClickListener val) {
            this.listener = val;
            return this;
        }

        public Builder duration(int duration) {
            this.duration = duration;
            return this;
        }

        public PermissionMessage build() {
            return new PermissionMessage(this);
        }
    }
}
