
## Real-Time Price Tracker App
An Android application built with Jetpack Compose, MVVM, and Clean Architecture that displays real-time stock price updates over a WebSocket connection.

##  Core Features
- Live Price Feed
- Periodically generates mock price updates for a configured list of stock symbols.
- Sends updates over a WebSocket echo server and processes the echoed messages.
- Start / Stop Feed
- Top bar toggle button to start or stop the price feed.
- Connection status clearly shown as 🟢 Connected / 🔴 Disconnected.
- Price List
- Scrollable list of stocks, sorted by current price (descending).
- Shows a placeholder (--) until the first price is received for a symbol.
- Visual Price Change Feedback
- Arrow indicators (↑ / ↓) for direction of price change.

## Screenshots

<img width="280" height="400" alt="Screenshot_20251202_121006" src="https://github.com/user-attachments/assets/2c7f5bd1-fd32-4d67-8436-38d2618f26a5" />

<img width="280" height="400" alt="Screenshot_20251202_121411" src="https://github.com/user-attachments/assets/e110e661-f05c-42f2-8f9a-7897396859c4" />

![Screen_recording_20251202_121322-ezgif com-video-to-gif-converter](https://github.com/user-attachments/assets/a71201f3-d052-496e-a298-852abb695476)

![Screen_recording_20251202_121436-ezgif com-video-to-gif-converter](https://github.com/user-attachments/assets/a4990d86-2398-4207-9ae1-128f4932c508)


## Technical Features
- Jetpack Compose
- Declarative UI with LazyColumn, Material3, and composable previews.
- Subtle row animations via animateItemPlacement and animateColorAsState.
- Clean Architecture
- Clear separation between:
- data: WebSocket adapter + repository implementation
- domain: repository interface + core models
- presentation: ViewModel, UI state, and composables
- MVVM + UDF

## Directory Structure
```
app/
├── MainActivity.kt                    # Entry point, sets Compose content
└── di/
    ├── NetworkModule.kt               # Provides OkHttpClient, WebSocket URL
    ├── RepositoryModule.kt            # Binds PriceRepository -> PriceRepositoryImpl
    └── AppCoroutineModule.kt          # Provides application-level CoroutineScope

core/
└── utils/
    └── StockConfig.kt                 # Holds configured list of symbols (e.g. 25 stocks)

data/
├── dto/
│   └── StockPriceDto.kt               # (Optional) network/transport model
├── remote/
│   └── PricingService.kt              # WebSocket adapter using OkHttp
├── repository/
│   └── PriceRepositoryImpl.kt         # Implements PriceRepository with random walk

domain/
├── models/
│   ├── StockPrice.kt                  # Domain entity: symbol, price, previousPrice, timestamp
│   ├── ConnectionStatus.kt            # Connected / Disconnected
│   ├── PriceChangeDirection.kt        # Up / Down / None
│   └── PriceFlashState.kt             # None / Up / Down
└── repository/
    └── PriceRepository.kt             # Domain interface used by ViewModel

presentation/
├── PriceViewModel.kt                  # Combines flows, manages feed + flash logic
├── PriceTrackerUiModels.kt            # PriceTrackerUiState, PriceRowUi, PriceList
├── PriceTrackerRoute.kt               # Route composable, wires ViewModel to UI
├── PriceTrackerScreen.kt              # Scaffold, top bar, list
├── PriceRow.kt                        # Row UI with flash animation
└── theme/
    ├── Color.kt
    ├── Theme.kt                       # PriceTrackerTheme
    └── Type.kt

tests/
├── unit/
│   ├── FakePriceRepository.kt         # Fake implementation for ViewModel tests
│   └── PriceViewModelTest.kt          # Toggle feed, flash logic, UI mapping
└── ui/
    └── PriceTrackerScreenTest.kt      # Compose UI tests using fake state


```
# Testing Strategy
Unit Tests

- PriceViewModelTest
 - Verifies initial UI state (placeholders for symbols).
 - Ensures toggling feed updates isFeedRunning.
 - Validates that price increases/decreases set PriceChangeDirection and PriceFlashState correctly.

- PriceRepositoryImplTest (optional)
 - Checks random walk logic and correct mapping of echoed messages.
 - UI Tests (Jetpack Compose)
 
- PriceTrackerScreenTest
Verifies:
 - “Start” / “Stop” button toggles text.
 - Connection status label shows Connected / Disconnected properly.
 - Rows display correct symbol and price text.

# Setup & Run
- Clone the repo
  - git clone https://github.com/your-username/realtime-price-tracker.git
  - cd realtime-price-tracker

- Build and run
  - ./gradlew assembleDebug
  - ./gradlew installDebug

# Platform Requirements
Platform	Version
Android	    Min SDK 24
            Target SDK 36
