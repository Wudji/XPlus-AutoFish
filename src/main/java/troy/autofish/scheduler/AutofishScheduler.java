package troy.autofish.scheduler;

import net.minecraft.client.Minecraft;
import troy.autofish.AutofishMod;

import java.util.ArrayList;
import java.util.List;

public class AutofishScheduler {

    private AutofishMod modAutofish;
    //Actions that run once then delete from queue
    private List<Action> queuedActions = new ArrayList<>();
    //Actions that repeat indefinitely
    private List<Action> repeatingActions = new ArrayList<>();

    //For tracking world change events. This is used to reset repeating action timers when a world is joined
    private boolean doesWorldExist;

    //ViewTurnStates
    private float originalYaw = 0.0f;
    private float originalPitch = 0.0f;
    private boolean isTurning = false;
    private boolean turnLeft = true;

    public AutofishScheduler(AutofishMod modAutofish) {
        this.modAutofish = modAutofish;
    }

    public void tick(Minecraft client) {

        //World change detection
        //This resets the timer on each repeating action on world change
        //Needed because Util.milliTime() can return a different value when the game is first initializing
        if ((client.level == null) == doesWorldExist) {
            doesWorldExist = (client.level != null);
            repeatingActions.forEach(Action::resetTimer);
        }

        //Clear out the action queue whenever Autofish is disabled or we are not ingame
        if (!modAutofish.getConfig().isAutofishEnabled()){
            queuedActions.clear();
            stopViewTurn();
        }
        //Clear out the action queue whenever world or player goes null
        //Also returns method to prevent NullPointers on any scheduled actions
        if (client.level == null || client.player == null) {
            queuedActions.clear();
            return;
        }

        //Check if any actions are ready to execute, remove if so
        queuedActions.removeIf(Action::tick);
        //Tick all repeating actions
        repeatingActions.forEach(Action::tick);

    }

    //If player caught a fish,then turn view
    public void onFishCaught() {
        if(modAutofish.getConfig().isAutofishEnabled() && !isTurning) {
            scheduleViewTurn();
        }
    }

    private void scheduleViewTurn() {
        Minecraft client = Minecraft.getInstance();
        if(client.player == null) return;
        if(!modAutofish.getConfig().isAutoTurnView()) return;

        //Save original yaw and pitch
        originalYaw = client.player.getYRot();
        originalPitch = client.player.getXRot();
        isTurning = true;

        //Schedule a view turn action
        scheduleAction(ActionType.TURN_VIEW, 0, () -> {
            if (client.player != null) {
                float turnAngle = modAutofish.getConfig().getTurnAngle();
                if (!turnLeft) {
                    turnAngle = -turnAngle;
                }
                float targetYaw = client.player.getYRot() + turnAngle;
                client.player.setYRot(targetYaw);
            }
        });

        //Schedule an action to finish the turn state after the turn duration, without resetting view
        scheduleAction(ActionType.RESET_VIEW, modAutofish.getConfig().getTurnDuration(), () -> {
            if (isTurning) {
                isTurning = false;
                turnLeft = !turnLeft;
            }
        });
    }

    public boolean isViewTurning() {return isTurning;}

    public void stopViewTurn(){
        if(isTurning) {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null) {
                client.player.setYRot(originalYaw);
                client.player.setXRot(originalPitch);
            }
            isTurning = false;

            // Clear any scheduled view turn actions
            queuedActions.removeIf(action ->
                    action.getActionType() == ActionType.TURN_VIEW ||
                            action.getActionType() == ActionType.RESET_VIEW
            );
        }
    }


    public void scheduleAction(ActionType actionType, long delay, Runnable runnable) {
        queuedActions.add(new Action(actionType, delay, runnable));
    }

    public void scheduleAction(Action action) {
        queuedActions.add(action);
    }

    public void scheduleRepeatingAction(long interval, Runnable runnable) {
        repeatingActions.add(new Action(ActionType.REPEATING_ACTION, interval, runnable));
    }

    public boolean isRecastQueued() {
        //True if any scheduled actions are of the RECAST type
        return queuedActions.stream().anyMatch(action -> action.getActionType() == ActionType.RECAST);
    }
}
