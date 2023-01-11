package mediasoupclientlibrary.audio;

import static android.media.AudioManager.MODE_IN_CALL;
import static android.media.AudioManager.MODE_IN_COMMUNICATION;
import static android.media.AudioManager.MODE_NORMAL;
import static android.media.AudioManager.MODE_RINGTONE;

import android.content.Context;
import android.media.AudioManager;

public class AudioUtils {
    public static void setSpeakerOn(Context context, Boolean speakerOn) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(speakerOn);
    }

    public static void setBluetoothOn(Context context, Boolean bluetoothOn) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (bluetoothOn) {
            audioManager.startBluetoothSco();
        } else {
            assert audioManager.isBluetoothScoAvailableOffCall();
            audioManager.stopBluetoothSco();
        }

        audioManager.setBluetoothScoOn(bluetoothOn);
    }

    public static boolean isSpeakerOn(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.isSpeakerphoneOn();
    }

    public static void setAudioMode(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }

    public static void restore(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(MODE_NORMAL);
    }

    public static String AudioManagerInfo(Context context) {
        StringBuilder sb = new StringBuilder();
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        sb.append("Mode:");
        sb.append(modeToString(audioManager.getMode())); // 检查扬声器是否打开
        sb.append(", ");

        sb.append("isSpeakerphoneOn:");
        sb.append(audioManager.isSpeakerphoneOn()); // 检查扬声器是否打开
        sb.append(", ");

        sb.append("isMicrophoneMute:");
        sb.append(audioManager.isMicrophoneMute());
        sb.append(", ");

        sb.append("isBluetoothA2dpOn:");
        sb.append(audioManager.isBluetoothA2dpOn()); // 检查A2DPAudio是否通过蓝牙耳机
        sb.append(", ");

        sb.append("isBluetoothScoOn:");
        sb.append(audioManager.isBluetoothScoOn());
        sb.append(", ");

        sb.append("isBluetoothScoAvailableOffCall:");
        sb.append(audioManager.isBluetoothScoAvailableOffCall());
        sb.append(", ");


        return sb.toString();
    }

    public static void setSpeakerphoneOn(boolean on, Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (on) {
            audioManager.setSpeakerphoneOn(true);
        } else {
            audioManager.setSpeakerphoneOn(false);//关闭扬声器
            audioManager.setRouting(AudioManager.MODE_IN_COMMUNICATION, AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL);
            //把声音设定成Earpiece（听筒）出来，设定为正在通话中
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        }
    }

    static String modeToString(int mode) {
        switch (mode) {
            case MODE_IN_CALL:
                return "MODE_IN_CALL";
            case MODE_IN_COMMUNICATION:
                return "MODE_IN_COMMUNICATION";
            case MODE_NORMAL:
                return "MODE_NORMAL";
            case MODE_RINGTONE:
                return "MODE_RINGTONE";
            default:
                return "MODE_INVALID";
        }
    }
}
