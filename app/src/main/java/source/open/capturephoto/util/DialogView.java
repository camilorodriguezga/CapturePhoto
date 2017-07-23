package source.open.capturephoto.util;


import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import source.open.capturephoto.R;

public class DialogView {

    static Dialog dialog = null;

    public static void showMessage(Context context, String title, String message) {
        dialog = new Dialog(context, R.style.CustomDialogTheme);
        dialog.setContentView(R.layout.alert_message);
        dialog.setCancelable(true);
        TextView tvText = (TextView) dialog.findViewById(R.id.tv_message);
        tvText.setText(message);
        acceptMessage();
        dialog.show();
    }

    private static void acceptMessage() {
        TextView tvAccept = (TextView) dialog.findViewById(R.id.btn_accept);
        tvAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public static void dismiss() {
        dialog.dismiss();
    }

    public static void acceptMessage(View.OnClickListener onClickListener) {
        TextView tvAccept = (TextView) dialog.findViewById(R.id.btn_accept);
        tvAccept.setOnClickListener(onClickListener);
    }
}
