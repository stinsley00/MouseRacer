use std::time::Instant;

use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum GameMode {
    Sprint,
    Endurance,
    Marathon,
}


#[derive(Debug)]
pub enum GameEvent {
    Tick {
        seconds: f32,
        total_clicks: u32,
        current_cps: f32,
    },
    Click {
        total_clicks: u32,
    },
    GameOver {
        taunt: String,
        total_clicks: u32,
        seconds: f32,
        avg_cps: f32,
        max_cps: f32,
    },
}

pub struct GameEngine {
    mode: GameMode,
    clicks_this_second: u32,
    total_clicks: u32,
    seconds: f32,
    start_seconds: f32,
    max_cps: f32,
    first_click: f32,
    adj_first_click: f32,
    running: bool,
    last_tick: Option<Instant>,
}

impl GameEngine {
    pub fn new() -> Self {
        Self {
            mode: GameMode::Sprint,
            clicks_this_second: 0,
            total_clicks: 0,
            seconds: 0.0,
            start_seconds: 0.0,
            max_cps: 0.0,
            first_click: 0.0,
            adj_first_click: 0.0,
            running: false,
            last_tick: None,
        }
    }

    pub fn start(&mut self, mode: GameMode) -> GameEvent {
        self.mode = mode;
        self.clicks_this_second = 0;
        self.total_clicks = 0;
        self.max_cps = 0.0;
        self.first_click = 0.0;
        self.adj_first_click = 0.0;
        self.running = true;

        self.seconds = match mode {
            GameMode::Sprint => 10.0,
            GameMode::Endurance | GameMode::Marathon => 0.0,
        };
        self.start_seconds = self.seconds;
        self.last_tick = Some(Instant::now());

        GameEvent::Tick {
            seconds: self.seconds,
            total_clicks: 0,
            current_cps: 0.0,
        }
    }

    pub fn register_click(&mut self) -> Option<GameEvent> {
        if self.running {
            self.total_clicks += 1;
            self.clicks_this_second += 1;
            Some(GameEvent::Click {
                total_clicks: self.total_clicks,
            })
        } else {
            None
        }
    }

    pub fn is_running(&self) -> bool {
        self.running
    }

    pub fn stop(&mut self) {
        self.running = false;
        self.last_tick = None;
    }

    /// Call each frame. Returns a tick event if a second has elapsed, or a game-over event.
    pub fn tick(&mut self) -> Option<GameEvent> {
        if !self.running {
            return None;
        }

        let Some(last) = self.last_tick else {
            return None;
        };

        if last.elapsed().as_secs_f32() < 1.0 {
            return None;
        }

        self.last_tick = Some(Instant::now());

        match self.mode {
            GameMode::Sprint => self.handle_sprint_tick(),
            GameMode::Endurance => self.handle_endurance_tick(),
            GameMode::Marathon => self.handle_marathon_tick(),
        }
    }

    fn handle_sprint_tick(&mut self) -> Option<GameEvent> {
        self.seconds -= 1.0;

        let current_cps = self.clicks_this_second as f32;
        self.clicks_this_second = 0;
        if current_cps > self.max_cps {
            self.max_cps = current_cps;
        }

        if self.seconds <= 0.0 {
            return Some(self.game_over());
        }

        Some(GameEvent::Tick {
            seconds: self.seconds,
            total_clicks: self.total_clicks,
            current_cps,
        })
    }

    fn handle_endurance_tick(&mut self) -> Option<GameEvent> {
        self.seconds += 1.0;

        let current_cps = self.clicks_this_second as f32;
        self.clicks_this_second = 0;
        if current_cps > self.max_cps {
            self.max_cps = current_cps;
        }

        if self.seconds >= 100.0 {
            return Some(self.game_over());
        }

        Some(GameEvent::Tick {
            seconds: self.seconds,
            total_clicks: self.total_clicks,
            current_cps,
        })
    }

    fn handle_marathon_tick(&mut self) -> Option<GameEvent> {
        self.seconds += 1.0;

        let current_cps = self.clicks_this_second as f32;
        self.clicks_this_second = 0;

        if self.first_click == 0.0 && current_cps > 0.0 {
            self.first_click = current_cps;
            self.adj_first_click = self.first_click * 0.75;
        }

        if current_cps > self.first_click * 1.05 {
            self.first_click = current_cps;
            self.adj_first_click = self.first_click * 0.75;
        }

        if self.first_click > 0.0 && current_cps <= self.adj_first_click {
            return Some(self.game_over());
        }

        if current_cps > self.max_cps {
            self.max_cps = current_cps;
        }

        Some(GameEvent::Tick {
            seconds: self.seconds,
            total_clicks: self.total_clicks,
            current_cps,
        })
    }

    fn game_over(&mut self) -> GameEvent {
        self.stop();
        let elapsed = if self.mode == GameMode::Sprint {
            self.start_seconds - self.seconds
        } else {
            self.seconds
        };
        let avg_cps = if elapsed > 0.0 {
            self.total_clicks as f32 / elapsed
        } else {
            0.0
        };
        GameEvent::GameOver {
            taunt: self.get_taunt(),
            total_clicks: self.total_clicks,
            seconds: elapsed,
            avg_cps,
            max_cps: self.max_cps,
        }
    }

    fn get_taunt(&self) -> String {
        let total = self.total_clicks;
        if total > 350 {
            "You Might Be Playing too much MouseRacer!"
        } else if total > 251 {
            "All time high score! NOT!"
        } else if total > 201 {
            "Unstoppable!"
        } else if total > 151 {
            "That call stack has your name written all over it!"
        } else if total > 121 {
            "You Have Taken The Lead"
        } else if total > 101 {
            "you have achieved hyperclick!"
        } else if total > 50 {
            "You make Chuck Norris Proud!"
        } else {
            "you might consider a different hobby"
        }
        .to_string()
    }
}
