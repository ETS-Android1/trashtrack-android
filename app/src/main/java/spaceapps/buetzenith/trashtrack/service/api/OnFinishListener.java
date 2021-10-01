package spaceapps.buetzenith.trashtrack.service.api;

public interface OnFinishListener<T>{
    void onSuccess(T t);
    void onFailure(Exception e);
}
