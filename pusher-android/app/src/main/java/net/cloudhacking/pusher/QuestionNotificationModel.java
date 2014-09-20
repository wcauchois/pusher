package net.cloudhacking.pusher;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

/**
 * Created by wcauchois on 9/20/14.
 * http://shri.blog.kraya.co.uk/2010/04/26/android-parcel-data-to-pass-between-activities-using-parcelable-classes/
 */
public class QuestionNotificationModel implements Parcelable {
    public static class ResponseOption implements Parcelable  {
        private String answer;
        private int code;

        public ResponseOption(Parcel in) {
            readFromParcel(in);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeString(getAnswer());
            parcel.writeInt(getCode());
        }

        private void readFromParcel(Parcel in) {
            answer = in.readString();
            code = in.readInt();
        }

        public static final Parcelable.Creator<ResponseOption> CREATOR =
                new Parcelable.Creator<ResponseOption>() {
            public ResponseOption createFromParcel(Parcel in) {
                return new ResponseOption(in);
            }

            public ResponseOption[] newArray(int size) {
                return new ResponseOption[size];
            }
        };

        public String getAnswer() {
            return answer;
        }

        public int getCode() {
            return code;
        }
    }

    public QuestionNotificationModel(Parcel in) {
        readFromParcel(in);
    }

    private void readFromParcel(Parcel in) {
        question = in.readString();
        questionId = in.readString();
        in.readList(options, ResponseOption.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(question);
        parcel.writeString(questionId);
        parcel.writeList(options);
    }

    public static final Parcelable.Creator<QuestionNotificationModel> CREATOR =
            new Parcelable.Creator<QuestionNotificationModel>() {
                public QuestionNotificationModel createFromParcel(Parcel in) {
                    return new QuestionNotificationModel(in);
                }

                public QuestionNotificationModel[] newArray(int size) {
                    return new QuestionNotificationModel[size];
                }
            };


    private String question;
    private List<ResponseOption> options;

    @SerializedName("question_id")
    private String questionId;

    public String getQuestion() {
        return question;
    }

    public String getQuestionId() {
        return questionId;
    }

    public List<ResponseOption> getOptions() {
        return options;
    }

    private QuestionNotificationModel() {
        // Can only be created from JSON for now.
    }

    public String toString() {
        String[] answers = new String[options.size()];
        for (int i = 0; i < options.size(); i++) {
            answers[i] = options.get(i).getAnswer();
        }
        return question + ": " + Arrays.toString(answers);
    }

    public static QuestionNotificationModel fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, QuestionNotificationModel.class);
    }
}
