package firestorewriter.troychuinard.com.firestorewriter;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore mFirestore;

    private static final String POLLS_LABEL = "Polls";
    private static final String VOTE_COUNT = "vote_count";

    private Handler h;

    private Button mStartButton;
    private Button mStopButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirestore = FirebaseFirestore.getInstance();

        mStartButton = (Button) findViewById(R.id.start_bot_button);
        mStopButton = (Button) findViewById(R.id.stop_bot_button);

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFirebaseVotingBots();

            }
        });

        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopFirebaseVotingBots();
            }
        });
    }


    private void startFirebaseVotingBots() {
        h = new Handler();
        final int delay = 1000; //milliseconds
        h.postDelayed(new Runnable() {
            public void run() {


                mFirestore.collection(POLLS_LABEL).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {

                        for (DocumentSnapshot d : querySnapshot) {
                            Log.v("DREF", d.getId());
                            mFirestore.collection(POLLS_LABEL).document(d.getId()).collection("answers").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot querySnapshot) {
                                    int numberOfAnswers = querySnapshot.size();
                                    Random rand = new Random();
                                    final int randomNumber = rand.nextInt((numberOfAnswers - 1) + 1) + 1;
                                    for (final DocumentSnapshot z : querySnapshot) {
                                        mFirestore.runTransaction(new com.google.firebase.firestore.Transaction.Function<Void>() {
                                            @Nullable
                                            @Override
                                            public Void apply(@NonNull com.google.firebase.firestore.Transaction transaction) throws FirebaseFirestoreException {
                                                DocumentSnapshot snapshot = transaction.get(z.getReference());
                                                double newCount = snapshot.getDouble(VOTE_COUNT) + randomNumber;
                                                transaction.update(z.getReference(), VOTE_COUNT, newCount);

                                                return null;
                                            }
                                        });

                                    }
                                }
                            });
                        }


                    }
                });
                h.postDelayed(this, delay);
            }
        }, delay);
    }

    private void stopFirebaseVotingBots() {
        h.removeCallbacksAndMessages(null);
    }

}


