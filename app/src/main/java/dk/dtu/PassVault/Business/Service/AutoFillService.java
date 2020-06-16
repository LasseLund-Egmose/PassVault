package dk.dtu.PassVault.Business.Service;

import android.app.assist.AssistStructure;
import android.os.Build;
import android.os.CancellationSignal;
import android.service.autofill.Dataset;
import android.service.autofill.FillCallback;
import android.service.autofill.FillContext;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveRequest;
import android.util.Log;
import android.view.autofill.AutofillId;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AutoFillService extends android.service.autofill.AutofillService {

    protected int identifyPasswordField(AssistStructure.ViewNode root) {
        if(root.getClassName().equals("android.widget.EditText")) {
            Log.i("Main", "Text: " + root.getText());
            Log.i("Main", "Hint: " + root.getHint());
        }

        for(int i = 0; i < root.getChildCount(); i++) {
            int fieldID = identifyPasswordField(root.getChildAt(i));
            if(fieldID > -1) {
                return fieldID;
            }
        }

        return -1;
    }

    @Override
    public void onFillRequest(FillRequest request, CancellationSignal signal, FillCallback callback) {
        Log.i("Autofill", "Autofill request!");

        List<FillContext> context = request.getFillContexts();
        AssistStructure structure = context.get(context.size() - 1).getStructure();

        ArrayList<Integer> passwordFields = new ArrayList<>();
        for(int i = 0; i < structure.getWindowNodeCount(); i++) {
            AssistStructure.WindowNode node = structure.getWindowNodeAt(i);
            Log.i("Autofill", "ID: " + node.getDisplayId() + ", Name: " + node.getTitle());

            int fieldID = this.identifyPasswordField(node.getRootViewNode());
            if(fieldID > -1) {
                passwordFields.add(fieldID);
            }
        }

        // TODO: Setup password RemoteViews

        // TODO: Build FillResponse

        // TODO: Run callback.onSuccess()
    }

    @Override
    public void onSaveRequest(@NonNull SaveRequest request, @NonNull SaveCallback callback) {
        // Not applicable
    }

}

class ParsedStructure {
    AutofillId usernameId;
    AutofillId passwordId;
}

class UserData {
    String username;
    String password;
}