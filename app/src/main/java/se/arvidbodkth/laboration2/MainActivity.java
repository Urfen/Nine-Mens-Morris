package se.arvidbodkth.laboration2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private NineMenMorrisRules model;
    private BoardView view;

    private int from;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        model = new NineMenMorrisRules();
        view = new BoardView(this, this);
        setContentView(view);
        from = 0;
    }

    @Override
    protected void onStart() {
        super.onStart();
       try{
            State state = (State) model.readFile(this.getApplicationContext());
            model = state.getModel();

            initGameFromFile(model.getBoard());

           view.invalidate();

            showToast("LOAD");
        } catch(IOException e){
            e.printStackTrace();
            //showToast("ERROR FAILED TO READ FROM FILE!");
        } catch(ClassNotFoundException c){
            showToast("FILE NOT FOUND!");
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        State state = new State(model);

        try{
            model.writeFile(this.getApplicationContext(), state);
            showToast("Saved");
        } catch(IOException e){
            e.printStackTrace();
            showToast("FAILED TO WRITE TO FILE!");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save ) {
            State state = new State(model);
            try{
                model.writeFile(this.getApplicationContext(), state);
                showToast("Saved");
            } catch(IOException e){
                e.printStackTrace();
                showToast("FAILED TO WRITE TO FILE!");
            }
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_load) {
            try{
                State state = (State) model.readFile(this.getApplicationContext());
                model = state.getModel();
                view.initGame(model.getBoard());
                view.rePaint();

                showToast("LOAD");
            } catch(IOException e){
                e.printStackTrace();
                //showToast("ERROR FAILED TO READ FROM FILE!");
            } catch(ClassNotFoundException c){
                showToast("FILE NOT FOUND!");
            }
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_restart) {
            model = new NineMenMorrisRules();
            view = new BoardView(this, this);
            setContentView(view);

            view.invalidate();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initGameFromFile(int[] gameState){
        for(int i = 1;i<view.getGamePieces().size()-1;i++){
            System.out.println("gameState " + gameState[i] + " i: " + i);
            view.getGamePieces().get(i).setColor(gameState[i+1]);
        }
    }

    public int getColorOfPos(int pos){
        return model.getColorOfPos(pos);
    }

    public int getTurn(){
        return model.getMarkerColor(model.getTurn());
    }

    public void placePiece(int pos) {

        placeMarkerAndRemove(pos);

        if(model.win(model.getMarkerColor(model.getLastTurn()))){
            model.nextTurn();
            String msg = model.turnToString() + " player Wins!!!!!";
            Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
            toast.show();
        }
        System.out.println(model.toString());
    }

    private void showToast(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    public boolean checkIfRemovable(int pos){
        if (model.getCanRemove()) {
            if (!model.clearSpace(pos, view.getColorOfPos(pos))) {
                view.setCurrentTurn(model.getTurn());
                showToast("Wrong pick!");
                return true;
            }
            System.out.println();
            view.paintEmptyPiece(pos, 0);
            model.nextTurn();
            view.setCurrentTurn(model.getTurn());
            from = 0;
            return true;
        }
        return false;
    }

    public void placeMarker(int pos){
        if (model.markersLeft(model.getTurn()) && view.getColorOfPos(pos) == 0) {
            from = 0;
            if (model.legalMove(pos, 0, model.getTurn())) {
                if (model.remove(pos)) {
                    model.setCanRemove(true);
                    view.paintEmptyPiece(pos, model.getTurn());
                    return;
                }
                view.paintEmptyPiece(pos, model.getTurn());
                System.out.println("Placed marker on: " + pos);
                if (!model.getCanRemove()) {
                    model.nextTurn();
                }
                if (model.getNoOfMarkers() > 0)
                    showToast(model.turnToString() + " has " + model.getNoOfMarkers() + " markers left to place");
                view.setCurrentTurn(model.getTurn());
            }
        } else {
            if (from != 0 && view.getColorOfPos(pos) == 0) {
                if (model.legalMove(pos, from, model.getTurn())) {

                    view.movePiece(pos, from, model.getTurn());
                    System.out.println("Moved a marker from: " + from + " to: " + pos);
                    if (model.remove(pos)) {
                        model.setCanRemove(true);
                    }
                    if (!model.getCanRemove()) {
                        model.nextTurn();
                    }
                    view.setCurrentTurn(model.getTurn());
                    from = 0;
                } else {
                    System.out.println("Failed to move marker from: " + from + " to: " + pos);
                    from = 0;
                }
            } else if (from == 0 && view.getColorOfPos(pos) == model.getTurn()) {
                from = pos;
                System.out.println("Saved: " + pos);
            } else {
                if (!model.getCanRemove()) {
                    showToast("Wrong imput, try again.");
                    from = 0;
                }

            }
        }
    }

    public void placeMarkerAndRemove(int pos){
        if(checkIfRemovable(pos)) return;
        placeMarker(pos);
    }
}
