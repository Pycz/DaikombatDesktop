package com.beverly.hills.money.gang.handler;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.proto.PushGameEventCommand;
import com.beverly.hills.money.gang.Configs;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.entities.enemies.Enemy;
import com.beverly.hills.money.gang.entities.enemies.EnemyPlayer;
import com.beverly.hills.money.gang.entities.enemies.EnemyPlayerAction;
import com.beverly.hills.money.gang.entities.enemies.EnemyPlayerActionType;
import com.beverly.hills.money.gang.registry.EnemiesRegistry;
import com.beverly.hills.money.gang.screens.PlayScreen;
import com.beverly.hills.money.gang.utils.Converter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class PlayScreenGameConnectionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(PlayScreenGameConnectionHandler.class);

    private final PlayScreen playScreen;

    private final EnemiesRegistry enemiesRegistry = new EnemiesRegistry();

    public void handle() {

        playScreen.getGameConnection().getResponse().poll().ifPresent(serverResponse -> {
            if (serverResponse.hasChatEvents()) {
                var chatEvent = serverResponse.getChatEvents();
                String playerName = enemiesRegistry.getEnemy(chatEvent.getPlayerId()).map(EnemyPlayer::getName).orElse("player");
                playScreen.getChatLog().addMessage(playerName, chatEvent.getMessage(),
                        () -> playScreen.getGame().getAssMan().getSound(SoundRegistry.PING).play(Constants.DEFAULT_SFX_VOLUME));
            } else if (serverResponse.hasGameEvents()) {

                var gameEvents = serverResponse.getGameEvents();
                if (gameEvents.hasPlayersOnline()) {
                    playScreen.setPlayersOnline(gameEvents.getPlayersOnline());
                }
                gameEvents.getEventsList().forEach(gameEvent -> {
                    switch (gameEvent.getEventType()) {
                        case SPAWN -> {
                            if (gameEvent.getPlayer().getPlayerId() == playScreen.getPlayerLoadedData().getPlayerId()) {
                                return;
                            } else if (enemiesRegistry.exists(gameEvent.getPlayer().getPlayerId())) {
                                // this might happen when players spawn at the same time
                                LOG.info("Player already spawned {}", gameEvent);
                                return;
                            }
                            LOG.info("SPAWN event {}", gameEvent);
                            EnemyPlayer enemyPlayer = new EnemyPlayer(playScreen.getPlayer(),
                                    gameEvent.getPlayer().getPlayerId(),
                                    new Vector3(gameEvent.getPlayer().getPosition().getX(),
                                            0.5057522f, gameEvent.getPlayer().getPosition().getY()),
                                    new Vector2(gameEvent.getPlayer().getDirection().getX(), gameEvent.getPlayer().getDirection().getY()),
                                    playScreen, gameEvent.getPlayer().getPlayerName(),
                                    enemy -> {
                                        int kills = playScreen.getPlayerKillLog().getKills() + 1;
                                        if (kills % 3 == 0) {
                                            playScreen.getGame().getAssMan().getSound(SoundRegistry.WINNING_SOUND_SEQ.getNextSound()).play(Constants.QUAKE_NARRATOR_FX_VOLUME);
                                        }
                                        playScreen.getGame().getAssMan().getSound(SoundRegistry.ENEMY_DEATH_SOUND_SEQ.getNextSound()).play(enemy.getSFXVolume());
                                    },
                                    enemy -> playScreen.getGame().getAssMan().getSound(SoundRegistry.ENEMY_GET_SHOT_SOUND_SEQ.getNextSound()).play(enemy.getSFXVolume()),
                                    enemy -> playScreen.getGame().getAssMan().getSound(SoundRegistry.SHOTGUN).play(enemy.getSFXVolume()));
                            playScreen.getGame().getEntMan().addEntity(enemyPlayer);
                            enemiesRegistry.addEnemy(gameEvent.getPlayer().getPlayerId(), enemyPlayer);

                        }
                        case EXIT -> {
                            if (gameEvent.getPlayer().getPlayerId() == playScreen.getPlayerLoadedData().getPlayerId()) {
                                return;
                            }
                            enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
                                    .map(EnemyPlayer::getName)
                                    .ifPresent(playerName -> {
                                        playScreen.getChatLog().addMessage("game log", playerName + " has left the game",
                                                () -> playScreen.getGame().getAssMan().getSound(SoundRegistry.PING).play(Constants.DEFAULT_SFX_VOLUME));
                                        enemiesRegistry.removeEnemy(gameEvent.getPlayer().getPlayerId())
                                                .ifPresent(Enemy::destroy);
                                    });
                        }
                        case DEATH -> {
                            if (gameEvent.getAffectedPlayer().getPlayerId() == playScreen.getPlayerLoadedData().getPlayerId()) {
                                String killedBy = enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
                                        .map(EnemyPlayer::getName).orElse("killer");
                                playScreen.getPlayer().die(killedBy,
                                        () -> {
                                            playScreen.getGame().getAssMan().getSound(SoundRegistry.LOOSING_SOUND_SEQ.getNextSound()).play(Constants.MK_NARRATOR_FX_VOLUME);
                                            playScreen.getGame().getAssMan().getSound(SoundRegistry.BELL).play(Constants.DEFAULT_SFX_VOLUME);
                                        });
                                enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
                                        .ifPresent(enemyPlayer -> enemyPlayer.queueAction(EnemyPlayerAction.builder()
                                                .enemyPlayerActionType(EnemyPlayerActionType.SHOOT)
                                                .direction(Converter.convertToVector2(gameEvent.getPlayer().getDirection()))
                                                .route(Converter.convertToVector2(gameEvent.getPlayer().getPosition()))
                                                .build()));
                            } else if (gameEvent.getPlayer().getPlayerId() == playScreen.getPlayerLoadedData().getPlayerId()) {
                                var victimPlayerOpt = enemiesRegistry.getEnemy(gameEvent.getAffectedPlayer().getPlayerId());
                                victimPlayerOpt.ifPresent(EnemyPlayer::die);
                                playScreen.getPlayerKillLog().myPlayerKill(victimPlayerOpt.map(EnemyPlayer::getName).orElse("victim"));
                            } else {
                                var victimPlayerOpt = enemiesRegistry.getEnemy(gameEvent.getAffectedPlayer().getPlayerId());
                                victimPlayerOpt.ifPresent(EnemyPlayer::die);
                                playScreen.getPlayerKillLog().otherPlayerKill(
                                        playScreen.getPlayerLoadedData().getPlayerName(),
                                        victimPlayerOpt.map(EnemyPlayer::getName).orElse("victim"));
                                enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
                                        .ifPresent(enemyPlayer -> enemyPlayer.queueAction(EnemyPlayerAction.builder()
                                                .enemyPlayerActionType(EnemyPlayerActionType.SHOOT)
                                                .direction(Converter.convertToVector2(gameEvent.getPlayer().getDirection()))
                                                .route(Converter.convertToVector2(gameEvent.getPlayer().getPosition())).build()));
                            }

                        }
                        case MOVE -> {
                            if (gameEvent.getPlayer().getPlayerId() == playScreen.getPlayerLoadedData().getPlayerId()) {
                                return;
                            }
                            enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
                                    .ifPresent(enemyPlayer -> enemyPlayer.queueAction(EnemyPlayerAction.builder()
                                            .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
                                            .direction(Converter.convertToVector2(gameEvent.getPlayer().getDirection()))
                                            .route(Converter.convertToVector2(gameEvent.getPlayer().getPosition())).build()));

                        }
                        case SHOOT -> {
                            // TODO don't send your own shoot event on the server side
                            if (gameEvent.getPlayer().getPlayerId() == playScreen.getPlayerLoadedData().getPlayerId()) {
                                return;
                            }
                            LOG.info("SHOOT event {}", gameEvent);

                            enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
                                    .ifPresent(enemyPlayer -> enemyPlayer.queueAction(EnemyPlayerAction.builder()
                                            .enemyPlayerActionType(EnemyPlayerActionType.SHOOT)
                                            .direction(Converter.convertToVector2(gameEvent.getPlayer().getDirection()))
                                            .route(Converter.convertToVector2(gameEvent.getPlayer().getPosition())).build()));
                        }
                        case GET_SHOT -> {
                            // if I shoot somebody, then do nothing. the animation is played one client immediately
                            if (gameEvent.getAffectedPlayer().getPlayerId() == playScreen.getPlayerLoadedData().getPlayerId()) {
                                // if I get shot
                                playScreen.getPlayer().getShot(gameEvent.getAffectedPlayer().getHealth(),
                                        () -> playScreen.getGame().getAssMan().getSound(SoundRegistry
                                                .GET_SHOT_SOUND_SEQ.getNextSound()).play(Constants.PLAYER_FX_VOLUME));
                            } else if (gameEvent.getPlayer().getPlayerId() != playScreen.getPlayerLoadedData().getPlayerId()) {
                                // enemies shooting each other
                                enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
                                        .ifPresent(enemyPlayer -> {
                                            enemyPlayer.queueAction(EnemyPlayerAction.builder()
                                                    .enemyPlayerActionType(EnemyPlayerActionType.SHOOT)
                                                    .direction(Converter.convertToVector2(gameEvent.getPlayer().getDirection()))
                                                    .route(Converter.convertToVector2(gameEvent.getPlayer().getPosition())).build());
                                        });
                                enemiesRegistry.getEnemy(gameEvent.getAffectedPlayer().getPlayerId())
                                        .ifPresent(EnemyPlayer::getShot);
                            }
                        }
                        case PING -> playScreen.getGameConnection().write(PushGameEventCommand.newBuilder()
                                .setPlayerId(playScreen.getPlayerLoadedData().getPlayerId())
                                .setGameId(Configs.GAME_ID)
                                .setEventType(PushGameEventCommand.GameEventType.PING).build());
                    }
                });
            }
        });
    }

}
