use std::fs;
use std::io;
use std::path::PathBuf;

use chrono::Local;
use serde::{Deserialize, Serialize};

use crate::game_engine::GameMode;


#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct HighScoreEntry {
    pub name: String,
    pub mode: GameMode,
    pub total_clicks: u32,
    pub seconds: f32,
    pub avg_cps: f32,
    pub max_cps: f32,
    pub date: String,
}

pub struct HighScoreBoard {
    entries: Vec<HighScoreEntry>,
    path: PathBuf,
}

impl HighScoreBoard {
    pub fn load() -> Self {
        let path = Self::scores_path();
        let entries = fs::read_to_string(&path)
            .ok()
            .and_then(|s| serde_json::from_str(&s).ok())
            .unwrap_or_default();
        Self { entries, path }
    }

    pub fn save(&self) -> Result<(), io::Error> {
        if let Some(parent) = self.path.parent() {
            fs::create_dir_all(parent)?;
        }
        let json = serde_json::to_string_pretty(&self.entries)?;
        fs::write(&self.path, json)
    }

    pub fn add_entry(&mut self, entry: HighScoreEntry) {
        self.entries.push(entry);
        self.entries
            .sort_by(|a, b| b.total_clicks.cmp(&a.total_clicks));
    }

    pub fn top_n(&self, mode: GameMode, n: usize) -> Vec<&HighScoreEntry> {
        self.entries
            .iter()
            .filter(|e| e.mode == mode)
            .take(n)
            .collect()
    }

    pub fn new_entry(
        name: String,
        mode: GameMode,
        total_clicks: u32,
        seconds: f32,
        avg_cps: f32,
        max_cps: f32,
    ) -> HighScoreEntry {
        HighScoreEntry {
            name,
            mode,
            total_clicks,
            seconds,
            avg_cps,
            max_cps,
            date: Local::now().format("%Y-%m-%dT%H:%M:%S").to_string(),
        }
    }

    fn scores_path() -> PathBuf {
        dirs::data_local_dir()
            .unwrap_or_else(|| PathBuf::from("."))
            .join("mouse-racer")
            .join("high_scores.json")
    }
}
