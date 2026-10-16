package com.example.domino6;

import android.Manifest;
import android.app.*;
import android.bluetooth.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.media.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    static final int REQ=40;
    static final UUID SPP=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothAdapter adapter; Host host; Client client;
    int[] score={0,0,0,0}; // half-points: 2=1.0, 1=0.5

    public void onCreate(Bundle b){super.onCreate(b); adapter=BluetoothAdapter.getDefaultAdapter(); requestBT(); menu();}
    void requestBT(){if(Build.VERSION.SDK_INT>=31&&checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT},REQ);}
    LinearLayout box(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(12,18,12,12);l.setBackgroundColor(Color.rgb(27,76,54));return l;}
    TextView t(String s,float z){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(Color.WHITE);v.setPadding(5,6,5,6);return v;}
    Button b(String s){Button x=new Button(this);x.setText(s);x.setTextSize(16);x.setAllCaps(false);x.setTextColor(Color.WHITE);x.setBackgroundColor(Color.rgb(190,145,45));return x;}
    void menu(){if(host!=null)host.close();if(client!=null)client.close();Arrays.fill(score,0);LinearLayout l=box();TextView h=t("DOMINÓ 6",30);h.setGravity(17);l.addView(h);TextView q=t("Doble-seis · sin Internet",17);q.setGravity(17);l.addView(q);Button cpu=b("Jugar contra la computadora");l.addView(cpu);cpu.setOnClickListener(v->new Local().start());Button bt=b("Jugar por Bluetooth");l.addView(bt);bt.setOnClickListener(v->btMenu());Button r=b("Reglas");l.addView(r);r.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Reglas").setMessage("28 fichas, 7 por jugador. Si puedes jugar, debes jugar. Si no puedes, pasas. Si se tranca, gana quien tenga menos puntos en sus fichas restantes. Empate: 0,5 puntos para cada empatado.").setPositiveButton("OK",null).show());setContentView(l);}
    void btMenu(){LinearLayout l=box();l.addView(t("JUEGO BLUETOOTH",27));for(int n=2;n<=4;n++){final int k=n;Button x=b(k+" jugadores");l.addView(x);x.setOnClickListener(v->hostJoin(k));}Button z=b("Volver");l.addView(z);z.setOnClickListener(v->menu());setContentView(l);}
    void hostJoin(int n){new AlertDialog.Builder(this).setTitle(n+" jugadores").setItems(new String[]{"Crear partida (anfitrión)","Unirse a partida"},(d,w)->{if(w==0){host=new Host(n);host.start();}else join(n);}).show();}
    void join(int n){try{ArrayList<BluetoothDevice> a=new ArrayList<>(adapter.getBondedDevices());if(a.isEmpty()){toast("Empareja primero los teléfonos en Ajustes > Bluetooth.");return;}String[] names=new String[a.size()];for(int i=0;i<a.size();i++)names[i]=a.get(i).getName();new AlertDialog.Builder(this).setTitle("Elegir anfitrión").setItems(names,(d,w)->{client=new Client(a.get(w));client.start();}).show();}catch(Exception e){toast("No se pudieron leer los dispositivos Bluetooth.");}}
    void toast(String s){runOnUiThread(()->Toast.makeText(this,s,Toast.LENGTH_SHORT).show());}
    String score(int x){return (x/2)+"."+(x%2==0?"0":"5");}

    static class D{int a,z;D(int a,int z){this.a=a;this.z=z;}D flip(){return new D(z,a);}int pts(){return a+z;}}
    static class G{int n,turn,opener,left,right,centerIndex;boolean first=true,done;int winner=-1,passes;String result="";ArrayList<ArrayList<D>> h=new ArrayList<>();ArrayList<D> board=new ArrayList<>();
        G(int n,int open){this.n=n;opener=open;turn=open;for(int i=0;i<n;i++)h.add(new ArrayList<>());ArrayList<D>d=new ArrayList<>();for(int a=0;a<7;a++)for(int z=a;z<7;z++)d.add(new D(a,z));Collections.shuffle(d);for(int k=0;k<7;k++)for(int p=0;p<n;p++)h.get(p).add(d.remove(0));}
        boolean vl(D d){return first||d.a==left||d.z==left||d.a==right||d.z==right;}boolean ll(D d){return first||d.a==left||d.z==left;}boolean rr(D d){return first||d.a==right||d.z==right;}boolean both(D d){return !first&&ll(d)&&rr(d);}boolean playable(int p){for(D d:h.get(p))if(vl(d))return true;return false;}
        void play(int p,int i,boolean L){
            if(p<0||p>=n||i<0||i>=h.get(p).size()||done)return;
            D d=h.get(p).get(i);
            if(!first){
                boolean canLeft=ll(d),canRight=rr(d);
                if(!canLeft&&!canRight)return;
                // La colocación nunca puede romper la unión de valores iguales.
                // Si llega una orden para un lado no válido, usa el único lado válido.
                if(L&&!canLeft)L=false;
                else if(!L&&!canRight)L=true;
            }
            d=h.get(p).remove(i);
            if(first){
                board.add(d);centerIndex=0;left=d.a;right=d.z;first=false;
            }else if(L){
                if(d.a==left){board.add(0,d.flip());left=d.z;}
                else{board.add(0,d);left=d.a;}
                centerIndex++;
            }else{
                if(d.a==right){board.add(d);right=d.z;}
                else{board.add(d.flip());right=d.a;}
            }
            passes=0;
            if(h.get(p).isEmpty()){done=true;winner=p;result="🏆 Gana Jugador "+(p+1)+" · se quedó sin fichas.";}
            else turn=(turn+1)%n;
        }
        void pass(){passes++;if(passes>=n){done=true;result=tranca();}else turn=(turn+1)%n;}
        int pts(int p){int s=0;for(D d:h.get(p))s+=d.pts();return s;}ArrayList<Integer> ties(){int m=999;for(int p=0;p<n;p++)m=Math.min(m,pts(p));ArrayList<Integer>w=new ArrayList<>();for(int p=0;p<n;p++)if(pts(p)==m)w.add(p);return w;}String tranca(){ArrayList<Integer>w=ties();if(w.size()==1)return"Tranca · gana Jugador "+(w.get(0)+1)+" con "+pts(w.get(0))+" puntos.";return"Tranca · empate entre "+names(w)+" con "+pts(w.get(0))+" puntos.";}String names(ArrayList<Integer>w){StringBuilder s=new StringBuilder();for(int i=0;i<w.size();i++){if(i>0)s.append(", ");s.append("Jugador ").append(w.get(i)+1);}return s.toString();}
    }
    void startSound(){tone(new int[]{660,880},new int[]{100,180});}
    // Sonido de ficha golpeando la mesa: golpe corto, con cuerpo grave y un pequeño chasquido.
    void tileSound(){new Thread(()->{try{int rate=22050;int ms=105;int n=rate*ms/1000;short[] data=new short[n];Random r=new Random();for(int i=0;i<n;i++){double t=i/(double)rate;double env=Math.exp(-t*34.0);double body=Math.sin(2*Math.PI*125*t)*0.72+Math.sin(2*Math.PI*235*t)*0.20;double click=(r.nextDouble()*2.0-1.0)*0.38*Math.exp(-t*75.0);double v=(body+click)*env*7200.0;data[i]=(short)Math.max(-32768,Math.min(32767,(int)v));}AudioTrack a=new AudioTrack(AudioManager.STREAM_MUSIC,rate,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT,data.length*2,AudioTrack.MODE_STATIC);a.write(data,0,data.length);a.play();Thread.sleep(ms+25);a.release();}catch(Exception ignored){}}).start();}
    void endSound(){tone(new int[]{880,660,440},new int[]{120,120,260});}
    void tone(int[] f,int[] ms){new Thread(()->{try{int rate=22050,total=0;for(int x:ms)total+=x;short[] data=new short[rate*total/1000];int pos=0;for(int k=0;k<f.length;k++){int c=rate*ms[k]/1000;for(int j=0;j<c&&pos<data.length;j++,pos++)data[pos]=(short)(Math.sin(2*Math.PI*f[k]*j/rate)*2600);}AudioTrack a=new AudioTrack(AudioManager.STREAM_MUSIC,rate,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT,data.length*2,AudioTrack.MODE_STATIC);a.write(data,0,data.length);a.play();Thread.sleep(total+30);a.release();}catch(Exception ignored){}}).start();}

    class TileView extends View{
        D d; Paint p=new Paint(1);
        TileView(D d){super(MainActivity.this);this.d=d;}
        protected void onDraw(Canvas c){
            float w=getWidth(),h=getHeight();
            p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(250,244,222));
            c.drawRoundRect(3,3,w-3,h-3,12,12,p);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(Color.rgb(91,58,32));
            c.drawRoundRect(3,3,w-3,h-3,12,12,p);
            p.setStrokeWidth(2);p.setColor(Color.rgb(150,130,100));
            c.drawLine(8,h/2,w-8,h/2,p);
            p.setStyle(Paint.Style.FILL);p.setColor(Color.BLACK);
            pips(c,d.a,0,h/2,w);pips(c,d.z,h/2,h,w);
        }
        void pips(Canvas c,int n,float y1,float y2,float w){
            float cx=w/2f,cy=(y1+y2)/2f;
            float dx=w*.25f,dy=(y2-y1)*.25f,r=4.5f;
            if(n==1)c.drawCircle(cx,cy,r,p);
            else if(n==2){c.drawCircle(cx-dx,cy-dy,r,p);c.drawCircle(cx+dx,cy+dy,r,p);}
            else if(n==3){c.drawCircle(cx-dx,cy-dy,r,p);c.drawCircle(cx,cy,r,p);c.drawCircle(cx+dx,cy+dy,r,p);}
            else if(n==4){for(int sx:new int[]{-1,1})for(int sy:new int[]{-1,1})c.drawCircle(cx+sx*dx,cy+sy*dy,r,p);}
            else if(n==5){for(int sx:new int[]{-1,1})for(int sy:new int[]{-1,1})c.drawCircle(cx+sx*dx,cy+sy*dy,r,p);c.drawCircle(cx,cy,r,p);}
            else if(n==6){for(int sx:new int[]{-1,1}){c.drawCircle(cx+sx*dx,cy-dy,r,p);c.drawCircle(cx+sx*dx,cy,r,p);c.drawCircle(cx+sx*dx,cy+dy,r,p);}}
        }
    }
    void board(LinearLayout l,G g,int player){
        // Marcador con la numeración de todos los jugadores, en su propio encabezado.
        StringBuilder sb=new StringBuilder("🏆 ");
        for(int i=0;i<g.n;i++){if(i>0)sb.append("  ·  ");sb.append("J").append(i+1).append(" ").append(score(score[i]));}
        TextView sc=t(sb.toString(),16);
        sc.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
        sc.setSingleLine(false);
        sc.setPadding(5,6,5,10);
        l.addView(sc,new LinearLayout.LayoutParams(-1,-2));
        BoardView bv=new BoardView(g);
        int side=Math.max(440,Math.min(getResources().getDisplayMetrics().widthPixels,560));
        l.addView(bv,new LinearLayout.LayoutParams(-1,side));
    }


    class BoardView extends View{
        G g; Paint p=new Paint(1);
        final float normalW=52f, normalH=34f;
        final float verticalW=34f, verticalH=52f;
        final float gap=0f;
        final float rowGap=0f;
        final float margin=6f;

        BoardView(G g){super(MainActivity.this);this.g=g;setBackgroundColor(Color.rgb(21,64,45));}

        protected void onDraw(Canvas c){
            super.onDraw(c);
            if(g.board.isEmpty())return;
            float cx=getWidth()/2f, cy=getHeight()/2f;
            int center=Math.max(0,Math.min(g.centerIndex,g.board.size()-1));
            D first=g.board.get(center);
            // Primera ficha jugada: horizontal en el centro.
            drawHorizontal(c,first.a,first.z,cx,cy);
            // Derecha: va a la derecha, sube, va a la izquierda, sube, va a la derecha...
            drawBranch(c,center+1,1,cx,cy);
            // Izquierda: va a la izquierda, baja, va a la derecha, baja, va a la izquierda...
            drawBranch(c,center-1,-1,cx,cy);
        }

        // step=+1: rama derecha (sube en los giros). step=-1: rama izquierda (baja en los giros).
        // Fichas pegadas, sin espacios. Filas: todas horizontales (dobles incluidos).
        // Giros: dos fichas verticales (dobles incluidos).
        void drawBranch(Canvas c,int index,int step,float cx,float cy){
            float W=getWidth();
            int rowDir=step;                 // +1 hacia la derecha, -1 hacia la izquierda
            int vs=step>0?-1:1;              // -1 sube, +1 baja
            float xEdge=cx+step*normalW/2f;  // borde libre de la última ficha
            float y=cy;
            boolean placedInRow=true;
            int guard=0;
            while(index>=0&&index<g.board.size()&&guard++<400){
                D d=g.board.get(index);
                // near = valor que toca la ficha anterior; far = valor del extremo abierto.
                int near=step>0?d.a:d.z, far=step>0?d.z:d.a;
                float left=rowDir>0?xEdge:xEdge-normalW;
                float right=left+normalW;
                if(left>=margin-0.01f&&right<=W-margin+0.01f){
                    float mx=(left+right)/2f;
                    if(rowDir>0)drawHorizontal(c,near,far,mx,y);   // near a la izquierda
                    else drawHorizontal(c,far,near,mx,y);          // near a la derecha
                    xEdge=rowDir>0?right:left;
                    index+=step;
                    placedInRow=true;
                }else{
                    if(!placedInRow)return; // ni una ficha cabe: evita bucle infinito
                    float colX=rowDir>0?xEdge-verticalW/2f:xEdge+verticalW/2f;
                    int vertCount=0;
                    for(int k=0;k<2&&index>=0&&index<g.board.size();k++){
                        D v=g.board.get(index);
                        int vn=step>0?v.a:v.z, vf=step>0?v.z:v.a;
                        float yc=y+vs*(normalH/2f+verticalH/2f+k*verticalH);
                        if(vs<0)drawVertical(c,vf,vn,colX,yc,true); // subiendo: near abajo
                        else drawVertical(c,vn,vf,colX,yc,true);    // bajando: near arriba
                        index+=step;
                        vertCount++;
                    }
                    y=y+vs*(normalH+vertCount*verticalH);
                    rowDir=-rowDir;
                    placedInRow=false;
                }
            }
        }

        void drawHorizontal(Canvas c,int leftValue,int rightValue,float cx,float cy){
            drawTile(c,new D(leftValue,rightValue),cx-normalW/2f,cy-normalH/2f,false);
        }

        void drawVertical(Canvas c,int topValue,int bottomValue,float cx,float cy,boolean connector){
            drawTile(c,new D(topValue,bottomValue),cx-verticalW/2f,cy-verticalH/2f,true);
        }

        float tileWidth(D d,boolean forceVertical){return forceVertical?verticalW:normalW;}
        float tileHeight(D d,boolean forceVertical){return forceVertical?verticalH:normalH;}

        void drawTile(Canvas c,D d,float x,float y,boolean forceVertical){
            float w=tileWidth(d,forceVertical),h=tileHeight(d,forceVertical);
            p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(250,244,222));
            c.drawRoundRect(x,y,x+w,y+h,5,5,p);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(Color.rgb(91,58,32));
            c.drawRoundRect(x,y,x+w,y+h,5,5,p);
            p.setStyle(Paint.Style.FILL);p.setColor(Color.BLACK);
            if(forceVertical){
                c.drawLine(x+4,y+h/2,x+w-4,y+h/2,p);
                drawPipsVertical(c,d.a,x,y,x+w,y+h/2);
                drawPipsVertical(c,d.z,x,y+h/2,x+w,y+h);
            }else{
                c.drawLine(x+w/2,y+4,x+w/2,y+h-4,p);
                drawPipsHorizontal(c,d.a,x,y,x+w/2,y+h);
                drawPipsHorizontal(c,d.z,x+w/2,y,x+w,y+h);
            }
        }

        // Horizontal: la distribución de los puntos sigue el eje horizontal.
        void drawPipsHorizontal(Canvas c,int n,float x1,float y1,float x2,float y2){
            float cx=(x1+x2)/2f,cy=(y1+y2)/2f;
            float dx=Math.max(3f,(x2-x1)*.25f),dy=Math.max(3f,(y2-y1)*.25f),r=3.0f;
            if(n==1)c.drawCircle(cx,cy,r,p);
            // Horizontal: 2 = abajo-izquierda + arriba-derecha.
            else if(n==2){c.drawCircle(cx-dx,cy+dy,r,p);c.drawCircle(cx+dx,cy-dy,r,p);}
            else if(n==3){c.drawCircle(cx-dx,cy+dy,r,p);c.drawCircle(cx,cy,r,p);c.drawCircle(cx+dx,cy-dy,r,p);}
            else if(n==4){for(int sx:new int[]{-1,1})for(int sy:new int[]{-1,1})c.drawCircle(cx+sx*dx,cy+sy*dy,r,p);}
            else if(n==5){for(int sx:new int[]{-1,1})for(int sy:new int[]{-1,1})c.drawCircle(cx+sx*dx,cy+sy*dy,r,p);c.drawCircle(cx,cy,r,p);}
            else if(n==6){for(int sy:new int[]{-1,1}){c.drawCircle(cx-dx,cy+sy*dy,r,p);c.drawCircle(cx,cy+sy*dy,r,p);c.drawCircle(cx+dx,cy+sy*dy,r,p);}}
        }

        // Vertical: la distribución de los puntos sigue el eje vertical.
        void drawPipsVertical(Canvas c,int n,float x1,float y1,float x2,float y2){
            float cx=(x1+x2)/2f,cy=(y1+y2)/2f;
            float dx=Math.max(3f,(x2-x1)*.25f),dy=Math.max(3f,(y2-y1)*.25f),r=3.0f;
            if(n==1)c.drawCircle(cx,cy,r,p);
            // Vertical: 2 = arriba-izquierda + abajo-derecha.
            else if(n==2){c.drawCircle(cx-dx,cy-dy,r,p);c.drawCircle(cx+dx,cy+dy,r,p);}
            else if(n==3){c.drawCircle(cx-dx,cy-dy,r,p);c.drawCircle(cx,cy,r,p);c.drawCircle(cx+dx,cy+dy,r,p);}
            else if(n==4){for(int sx:new int[]{-1,1})for(int sy:new int[]{-1,1})c.drawCircle(cx+sx*dx,cy+sy*dy,r,p);}
            else if(n==5){for(int sx:new int[]{-1,1})for(int sy:new int[]{-1,1})c.drawCircle(cx+sx*dx,cy+sy*dy,r,p);c.drawCircle(cx,cy,r,p);}
            else if(n==6){for(int sx:new int[]{-1,1}){c.drawCircle(cx+sx*dx,cy-dy,r,p);c.drawCircle(cx+sx*dx,cy,r,p);c.drawCircle(cx+sx*dx,cy+dy,r,p);}}
        }
    }
    void hand(LinearLayout l,G g,int p,boolean clickable,OnTile action){
        l.addView(t("Tus fichas",16));
        HorizontalScrollView s=new HorizontalScrollView(this);
        s.setFillViewport(true);
        s.setHorizontalScrollBarEnabled(false);

        LinearLayout r=new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER);
        r.setPadding(8,0,8,0);

        // El contenido queda centrado cuando cabe y sigue siendo desplazable
        // cuando no cabe en la pantalla.
        r.setMinimumWidth(getResources().getDisplayMetrics().widthPixels-16);

        for(int i=0;i<g.h.get(p).size();i++){
            int ix=i;
            TileView v=new TileView(g.h.get(p).get(i));
            if(clickable)v.setOnClickListener(x->action.go(ix,g.h.get(p).get(ix)));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(70,120);
            lp.setMargins(3,0,3,0);
            r.addView(v,lp);
        }
        s.addView(r,new HorizontalScrollView.LayoutParams(-2,130));
        l.addView(s,new LinearLayout.LayoutParams(-1,130));
    }
    interface OnTile{void go(int i,D d);}

    void cornerScore(LinearLayout l,String title,int pts){
        FrameLayout f=new FrameLayout(this);
        TextView h=t(title,24); f.addView(h,new FrameLayout.LayoutParams(-1,-2));
        TextView sc=t("🏆 "+score(pts),14); sc.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams sp=new FrameLayout.LayoutParams(78,40,Gravity.TOP|Gravity.RIGHT);
        f.addView(sc,sp); l.addView(f);
    }

    class Local{G g;boolean counted;
        void start(){g=new G(2,0);counted=false;startSound();draw();}
        void draw(){LinearLayout l=box();l.addView(t(g.done?g.result:(g.turn==0?"Tu turno":"Turno de la computadora"),17));board(l,g,0);hand(l,g,0,!g.done,(i,d)->{if(g.turn!=0||g.done||!g.vl(d))return;if(g.both(d))choose(i,d);else{g.play(0,i,g.ll(d));tileSound();after();}});Button pass=b("Pasar");l.addView(pass);pass.setOnClickListener(v->{if(g.turn==0&&!g.done&&!g.playable(0)){g.pass();after();}});if(g.done){Button nn=b("Nueva mano");l.addView(nn);nn.setOnClickListener(v->newHand());}Button m=b("Salir al menú");l.addView(m);m.setOnClickListener(v->menu());setContentView(l);}
        void choose(int i,D d){new AlertDialog.Builder(MainActivity.this).setTitle("¿Dónde colocar la ficha?").setItems(new String[]{"Izquierda","Derecha"},(x,w)->{g.play(0,i,w==0);tileSound();after();}).show();}
        void after(){
            if(g.done){finishScore();draw();return;}
            computerTurn();
            if(g.done)finishScore();
            draw();
        }
        void computerTurn(){
            // La computadora debe resolver todos sus turnos automáticamente.
            // Esto también cubre una nueva mano abierta por ella.
            int guard=0;
            while(!g.done&&g.turn==1&&guard++<20){
                int i=-1;
                for(int k=0;k<g.h.get(1).size();k++){
                    if(g.vl(g.h.get(1).get(k))){i=k;break;}
                }
                if(i<0){
                    g.pass();
                    continue;
                }
                D cd=g.h.get(1).get(i);
                boolean useLeft=g.ll(cd);
                boolean useRight=g.rr(cd);
                boolean side=useLeft&&!useRight;
                if(!useLeft&&useRight)side=false;
                // Si ambos extremos sirven, se usa la derecha de forma determinista.
                g.play(1,i,side);
                tileSound();
            }
        }
        void finishScore(){if(counted)return;counted=true;endSound();if(g.winner>=0)score[g.winner]+=2;else for(int p:g.ties())score[p]+=1;}
        void newHand(){
            ArrayList<Integer>w=g.winner>=0?new ArrayList<>(Collections.singletonList(g.winner)):g.ties();
            int next=w.contains(g.opener)?g.opener:w.get(new Random().nextInt(w.size()));
            g=new G(2,next);counted=false;startSound();
            // Si la nueva mano la abre la computadora, ella sale sin esperar un toque.
            if(g.turn==1)computerTurn();
            draw();
        }
    }

    class Host{int n;G g;BluetoothServerSocket ss;ArrayList<Peer> peers=new ArrayList<>();boolean running;Host(int n){this.n=n;}void start(){running=true;new Thread(()->{try{ss=adapter.listenUsingRfcommWithServiceRecord("Domino6 Offline BT",SPP);runOnUiThread(()->waiting());while(running&&peers.size()<n-1){BluetoothSocket s=ss.accept();Peer p=new Peer(s,peers.size()+1);peers.add(p);p.start();}if(peers.size()==n-1){g=new G(n,0);startSound();broadcast();}}catch(Exception e){toast("Bluetooth: "+e.getMessage());}}).start();}void waiting(){LinearLayout l=box();l.addView(t("ANFITRIÓN",27));l.addView(t("Esperando jugadores…",18));Button m=b("Volver al menú");l.addView(m);m.setOnClickListener(v->{close();menu();});setContentView(l);}void broadcast(){if(g==null)return;for(Peer p:peers)p.send(encode(g,p.id));runOnUiThread(this::draw);}
        String encode(G x,int recipient){StringBuilder s=new StringBuilder("STATE|").append(x.n).append(';').append(x.turn).append(';').append(x.opener).append(';').append(x.first?1:0).append(';').append(x.left).append(';').append(x.right).append(';').append(x.done?1:0).append(';').append(x.winner).append(';').append(x.result.replace("|","/")).append(';').append(x.centerIndex).append(';').append(score[Math.max(0,Math.min(recipient,score.length-1))]).append(';').append(score[0]).append(',').append(score[1]).append(',').append(score[2]).append(',').append(score[3]);s.append('|');for(D d:x.board)s.append(d.a).append(',').append(d.z).append(',');s.append('|');for(ArrayList<D> h:x.h){for(D d:h)s.append(d.a).append(',').append(d.z).append(',');s.append('/');}return s.toString();}
        void action(int p,String c){if(g==null)return;if(c.startsWith("PLAY|")&&!g.done&&g.turn==p){String[]q=c.split("\\|");int i=Integer.parseInt(q[1]);boolean L=q.length<3||q[2].equals("L");if(i>=0&&i<g.h.get(p).size()&&g.vl(g.h.get(p).get(i))){g.play(p,i,L);tileSound();if(g.done)finish();else broadcast();}}else if(c.equals("PASS")&&!g.done&&g.turn==p&&!g.playable(p)){g.pass();if(g.done)finish();else broadcast();}else if(c.equals("NEW_HAND")&&p==0&&g.done)newHand();}
        void finish(){endSound();if(g.winner>=0)score[g.winner]+=2;else for(int p:g.ties())score[p]+=1;broadcast();}void newHand(){ArrayList<Integer>w=g.winner>=0?new ArrayList<>(Collections.singletonList(g.winner)):g.ties();int next=w.contains(g.opener)?g.opener:w.get(new Random().nextInt(w.size()));g=new G(n,next);startSound();broadcast();}
        void draw(){LinearLayout l=box();l.addView(t("Jugador 1",17));l.addView(t(g.done?g.result:(g.turn==0?"Tu turno":"Turno del Jugador "+(g.turn+1)),17));board(l,g,0);hand(l,g,0,true,(i,d)->{if(g.done||g.turn!=0||!g.vl(d))return;if(g.both(d))new AlertDialog.Builder(MainActivity.this).setTitle("¿Dónde colocar la ficha?").setItems(new String[]{"Izquierda","Derecha"},(x,w)->action(0,"PLAY|"+i+"|"+(w==0?"L":"R"))).show();else action(0,"PLAY|"+i+"|"+(g.ll(d)?"L":"R"));});Button p=b("Pasar");l.addView(p);p.setOnClickListener(v->action(0,"PASS"));if(g.done){Button nn=b("Nueva mano");l.addView(nn);nn.setOnClickListener(v->action(0,"NEW_HAND"));}Button m=b("Salir al menú");l.addView(m);m.setOnClickListener(v->{close();menu();});setContentView(l);}
        void close(){running=false;try{if(ss!=null)ss.close();}catch(Exception ignored){}for(Peer p:peers)p.close();}class Peer{BluetoothSocket s;BufferedReader in;PrintWriter out;int id;Peer(BluetoothSocket s,int id)throws Exception{this.s=s;this.id=id;in=new BufferedReader(new InputStreamReader(s.getInputStream()));out=new PrintWriter(s.getOutputStream(),true);}void start(){new Thread(()->{try{out.println("ID|"+id+"|"+n);String z;while((z=in.readLine())!=null)action(id,z);}catch(Exception ignored){}}).start();}void send(String z){out.println(z);}void close(){try{s.close();}catch(Exception ignored){}}}
    }

    class Client{BluetoothDevice dev;BluetoothSocket s;BufferedReader in;PrintWriter out;int me=-1,n;G g;Client(BluetoothDevice d){dev=d;}void start(){new Thread(()->{try{s=dev.createRfcommSocketToServiceRecord(SPP);s.connect();in=new BufferedReader(new InputStreamReader(s.getInputStream()));out=new PrintWriter(s.getOutputStream(),true);runOnUiThread(()->showClient("Conectado. Esperando partida…"));String z;while((z=in.readLine())!=null)decode(z);}catch(Exception e){toast("No se pudo conectar: "+e.getMessage());}}).start();}void decode(String z){if(z.startsWith("ID|")){me=Integer.parseInt(z.split("\\|")[1]);n=Integer.parseInt(z.split("\\|")[2]);}else if(z.startsWith("STATE|")){int oldBoard=g==null?-1:g.board.size();int oldHand=(g==null||me<0)?-1:g.h.get(Math.min(me,g.n-1)).size();parse(z.substring(6));if(oldBoard>=0&&g.board.size()>oldBoard&&oldHand>=0&&g.h.get(Math.min(me,g.n-1)).size()<oldHand)tileSound();runOnUiThread(()->showClient(""));}}void parse(String x){try{String[]a=x.split("\\|",-1);String[]q=a[0].split(";");int N=Integer.parseInt(q[0]);g=new G(N,Integer.parseInt(q[2]));g.turn=Integer.parseInt(q[1]);g.first=q[3].equals("1");g.left=Integer.parseInt(q[4]);g.right=Integer.parseInt(q[5]);g.done=q[6].equals("1");g.winner=Integer.parseInt(q[7]);g.result=q[8];g.centerIndex=q.length>9?Integer.parseInt(q[9]):0;if(q.length>10)score[Math.max(0,Math.min(me,score.length-1))]=Integer.parseInt(q[10]);if(q.length>11){String[] ss=q[11].split(",");for(int i=0;i<ss.length&&i<score.length;i++)score[i]=Integer.parseInt(ss[i]);}String[]hs=a[2].split("/");for(int p=0;p<N;p++){g.h.get(p).clear();if(p<hs.length){String[]ts=hs[p].split(",");for(int i=0;i+1<ts.length;i+=2)if(!ts[i].isEmpty())g.h.get(p).add(new D(Integer.parseInt(ts[i]),Integer.parseInt(ts[i+1])));}}String[]bs=a[1].split(",");g.board.clear();for(int i=0;i+1<bs.length;i+=2)if(!bs[i].isEmpty())g.board.add(new D(Integer.parseInt(bs[i]),Integer.parseInt(bs[i+1])));if(!g.board.isEmpty()){g.first=false;g.left=g.board.get(0).a;g.right=g.board.get(g.board.size()-1).z;}}catch(Exception ignored){}}void showClient(String info){LinearLayout l=box();if(g==null){l.addView(t(info,18));Button m=b("Salir");l.addView(m);m.setOnClickListener(v->{close();menu();});setContentView(l);return;}l.addView(t("Jugador "+(me+1),17));l.addView(t(g.done?g.result:(g.turn==me?"Tu turno":"Turno del Jugador "+(g.turn+1)),17));board(l,g,Math.max(0,Math.min(me,score.length-1)));int p=Math.max(0,Math.min(me,g.n-1));hand(l,g,p,true,(i,d)->{if(g.done||g.turn!=me||!g.vl(d))return;if(g.both(d))new AlertDialog.Builder(MainActivity.this).setTitle("¿Dónde colocar la ficha?").setItems(new String[]{"Izquierda","Derecha"},(x,w)->{out.println("PLAY|"+i+"|"+(w==0?"L":"R"));}).show();else{out.println("PLAY|"+i+"|"+(g.ll(d)?"L":"R"));}});Button pss=b("Pasar");l.addView(pss);pss.setOnClickListener(v->{if(!g.done&&g.turn==me&&!g.playable(me))out.println("PASS");});Button m=b("Salir al menú");l.addView(m);m.setOnClickListener(v->{close();menu();});setContentView(l);}void close(){try{s.close();}catch(Exception ignored){}}}
}
