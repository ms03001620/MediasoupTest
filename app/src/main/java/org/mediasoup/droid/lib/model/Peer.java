package org.mediasoup.droid.lib.model;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
public class Peer extends Info {

  private String mId;
  private String mDisplayName;
  private DeviceInfo mDevice;

  private Set<String> consumers;

  //{"id":"rvunszs6","displayName":"Name:honor_red","device":{"flag":"android","name":"Android HWBKL","version":"REL"}}
  public Peer(@NonNull JSONObject info) {
    mId = info.optString("id");
    mDisplayName = info.optString("displayName");
    JSONObject deviceInfo = info.optJSONObject("device");
    if (deviceInfo != null) {
      mDevice =
          new DeviceInfo()
              .setFlag(deviceInfo.optString("flag"))
              .setName(deviceInfo.optString("name"))
              .setVersion(deviceInfo.optString("version"));
    } else {
      mDevice = DeviceInfo.unknownDevice();
    }
    consumers = new HashSet<>();
  }

  @Override
  public String getId() {
    return mId;
  }

  @Override
  public String getDisplayName() {
    return mDisplayName;
  }

  @Override
  public DeviceInfo getDevice() {
    return mDevice;
  }

  public void setDisplayName(String displayName) {
    this.mDisplayName = displayName;
  }

  public void setDevice(DeviceInfo device) {
    this.mDevice = device;
  }

  public Set<String> getConsumers() {
    return consumers;
  }

  @Override
  public String toString() {
    return "Peer{" +
            "mId='" + mId + '\'' +
            ", mDisplayName='" + mDisplayName + '\'' +
            ", mDevice=" + mDevice +
            ", consumers=" + consumers +
            '}';
  }
}
