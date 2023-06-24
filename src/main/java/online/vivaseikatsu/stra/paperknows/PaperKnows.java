package online.vivaseikatsu.stra.paperknows;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.EntityBlockStorage;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public final class PaperKnows extends JavaPlugin implements Listener {


    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getServer().getPluginManager().registerEvents(this,this);
        getLogger().info("プラグインが有効になりました。");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("プラグインが無効になりました。");
    }

    // エンティティに右クリックしたとき
    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent e) {
        // playerを取得
        Player p = e.getPlayer();
        // スニークしてる場合のみ続行
        if(!p.isSneaking()) return;

        // メインハンドのアイテムを取得
        ItemStack mainHand = p.getInventory().getItemInMainHand();
        // メインハンドに持っているのが紙じゃなかった場合、終了
        if(!(mainHand.getType() == Material.PAPER)) return;

        // クールタイム以内だった場合、終了
        if(!doCooldown(p,500)) return;

        // 右クリックしたエンティティを取得
        Entity entity = e.getRightClicked();

        // 手懐け可能Mobのとき
        if(entity instanceof Tameable){
            ShowPetInfo(p,entity);
            return;
        }

        // ダメージを受けるMobのとき
        if(entity instanceof Damageable){
            ShowMobInfo(p,entity);
            return;
        }


        // エンティティ右クリックここまで
    }

    // ブロックを右クリックしたとき
    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e){

        // ブロックを右クリックじゃなかったら、終了
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        // 右クリックしたブロックが取得できない場合は、終了
        if(e.getClickedBlock() == null) return;

        // playerを取得
        Player p = e.getPlayer();
        // スニークしてる場合のみ続行
        if(!p.isSneaking()) return;

        // メインハンドのアイテムを取得
        ItemStack mainHand = p.getInventory().getItemInMainHand();
        // メインハンドに持っているのが紙じゃなかった場合、終了
        if(!(mainHand.getType() == Material.PAPER)) return;

        // クールタイム以内だった場合、終了
        if(!doCooldown(p,500)) return;

        // 右クリックしたブロックを取得
        Block block = e.getClickedBlock();

        // 対象が蜂の巣だった場合
        if(block.getBlockData() instanceof Beehive){
            ShowBeehiveInfo(p,block);
            return;
        }

        // 対象がレベルをもつブロックだった場合
        if(block.getBlockData() instanceof Levelled){
            ShowLevelledBlockInfo(p,block);
            return;
        }



    }










    // ---- ここから細々した処理たち ----


    // クールダウン用ハッシュマップを作成
    public HashMap<String, Long> cooldownMap = new HashMap<String, Long>();

    // クールダウン用の処理
    public boolean doCooldown(Player player, int cooldownTime){

        // プレイヤーがハッシュマップに含まれている場合
        if(cooldownMap.containsKey(player.getName())){

            // 前回の時間からクールダウンタイム満了までの時間(秒)を算出
            long waitTime = cooldownMap.get(player.getName()) - System.currentTimeMillis() + cooldownTime;

            // 待ち時間が残り0秒以上だった場合
            if(waitTime>0){
                return false;
            }

        }

        //　クールダウンで蹴られなかった場合は、今回の実行時間を記録
        cooldownMap.put(player.getName(), System.currentTimeMillis());

        return true;

        // クールダウン用の処理ここまで
    }


    //　手懐け可能Mobの情報を表示
    private void ShowPetInfo(Player player,Entity entity){

        // 手懐けできるエンティティじゃなかったら終了
        if(!(entity instanceof Tameable)) return;
        Tameable pet = (Tameable) entity;

        // タイトル
        player.sendMessage(ChatColor.GRAY + "--[ Mob Status ]--");
        // Mobの種類
        player.sendMessage(ChatColor.WHITE + "MobType: " + pet.getType());
        // 名前
        if(pet.getCustomName() == null){
            player.sendMessage(ChatColor.WHITE + "Name: " + ChatColor.GRAY + "-");
        } else {
            player.sendMessage(ChatColor.WHITE + "Name: " + pet.getCustomName());
        }
        // 体力
        player.sendMessage(ChatColor.WHITE + "HP: " + (int) pet.getHealth() +
                " / " + (int) pet.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        // オーナー
        if(pet.getOwner() == null){
            player.sendMessage(ChatColor.WHITE + "Owner: " + ChatColor.GRAY + "-");
        } else {
            player.sendMessage(ChatColor.WHITE + "Owner: " + pet.getOwner().getName());
        }
        // 座標
        player.sendMessage(ChatColor.GRAY + "(" + pet.getLocation().getBlockX() +","+ pet.getLocation().getBlockY() +","+ pet.getLocation().getBlockZ()+")");

    // ペット情報ここまで
    }


    // Mob情報を表示
    private void ShowMobInfo(Player player,Entity entity){

        // ダメージを受けるMobじゃなかった場合、終了
        if( !(entity instanceof Damageable) ) return;
        if( !(entity instanceof Attributable) ) return;
        Damageable mob = (Damageable) entity;
        Attributable health = (Attributable) entity;

        // タイトル
        player.sendMessage(ChatColor.GRAY + "--[ Mob Status ]--");
        // Mobの種類
        player.sendMessage(ChatColor.WHITE + "MobType: " + mob.getType());
        // 名前
        if(mob.getCustomName() == null){
            player.sendMessage(ChatColor.WHITE + "Name: " + ChatColor.GRAY + "-");
        } else {
            player.sendMessage(ChatColor.WHITE + "Name: " + mob.getCustomName());
        }
        // 体力
        player.sendMessage(ChatColor.WHITE + "HP: " + (int) mob.getHealth() +
                " / " + (int) health.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        // 座標
        player.sendMessage(ChatColor.GRAY + "(" + mob.getLocation().getBlockX() +","+ mob.getLocation().getBlockY() +","+ mob.getLocation().getBlockZ()+")");






    }


    // 蜂の巣の情報を表示
    private void ShowBeehiveInfo(Player player,Block block){

        // 蜂の巣じゃなかったら終了
        if( !(block.getBlockData() instanceof Beehive) ) return;
        if( !(block.getState() instanceof org.bukkit.block.Beehive) ) return;

        // Beehiveを取得
        Beehive beeData = (Beehive) block.getBlockData();
        org.bukkit.block.Beehive beeBlock = (org.bukkit.block.Beehive) block.getState();


        // タイトル
        player.sendMessage(ChatColor.GRAY + "--[ BeeHive Status ]--");
        // ハチの数
        player.sendMessage(ChatColor.WHITE + "Num of Bee: " + beeBlock.getEntityCount() + " / 3");
        // はちみつレベル
        player.sendMessage(ChatColor.WHITE + "HoneyLevel: " + beeData.getHoneyLevel() + " / " + beeData.getMaximumHoneyLevel());
        // 鎮静されているか
        player.sendMessage(ChatColor.WHITE + "Sedated: " + beeBlock.isSedated());
        // 座標
        player.sendMessage(ChatColor.GRAY + "(" + block.getX() +","+ block.getY() +","+ block.getZ() + ")");

    // 蜂の巣情報ここまで
    }

    // レベルブロックの情報を表示
    private void ShowLevelledBlockInfo(Player player,Block block){

        // レベルブロックじゃなかったら終了
        if( !(block.getBlockData() instanceof Levelled) ) return;

        // ブロックデータの取得
        Levelled levelled = (Levelled) block.getBlockData();

        // タイトル
        player.sendMessage(ChatColor.GRAY + "--[ LevelledBlock Status ]--");
        // タイプ
        player.sendMessage(ChatColor.WHITE + "Type: " + block.getType());
        // レベル
        player.sendMessage(ChatColor.WHITE + "Level: " + levelled.getLevel() + " / " + levelled.getMaximumLevel());
        // 座標
        player.sendMessage(ChatColor.GRAY + "(" + block.getX() +","+ block.getY() +","+ block.getZ() + ")");

        // レベルブロックここまで
    }




}