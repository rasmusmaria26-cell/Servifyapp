import os
import shutil
import re

# Base directory
BASE_DIR = os.path.join("app", "src", "main", "java", "com", "servify", "app")

MOVES = {
    # Core
    "data/remote/GeminiApiClient.kt": "core/network/GeminiApiClient.kt",
    "data/remote/SupabaseClient.kt": "core/network/SupabaseClient.kt",
    "data/model/AIDiagnosis.kt": "core/model/AIDiagnosis.kt",

    # DesignSystem
    "ui/theme/Color.kt": "designsystem/theme/Color.kt",
    "ui/theme/ExtendedColors.kt": "designsystem/theme/ExtendedColors.kt",
    "ui/theme/ServifyTextStyles.kt": "designsystem/theme/ServifyTextStyles.kt",
    "ui/theme/Theme.kt": "designsystem/theme/Theme.kt",
    "ui/theme/Type.kt": "designsystem/theme/Type.kt",
    "presentation/components/ServifyButton.kt": "designsystem/ServifyButton.kt",
    "presentation/components/Shimmer.kt": "designsystem/Shimmer.kt",
    "presentation/components/PremiumComponents.kt": "designsystem/PremiumComponents.kt",

    # Auth feature
    "repository/AuthRepository.kt": "feature/auth/data/AuthRepository.kt",
    "data/model/User.kt": "feature/auth/domain/User.kt",
    "presentation/auth/LoginScreen.kt": "feature/auth/presentation/LoginScreen.kt",
    "presentation/auth/LoginViewModel.kt": "feature/auth/presentation/LoginViewModel.kt",
    "presentation/auth/SignupScreen.kt": "feature/auth/presentation/SignupScreen.kt",
    "presentation/auth/SignupViewModel.kt": "feature/auth/presentation/SignupViewModel.kt",

    # Customer feature
    "repository/BookingRepository.kt": "feature/customer/data/BookingRepository.kt",
    "repository/RepairRepository.kt": "feature/customer/data/RepairRepository.kt",
    "data/model/Booking.kt": "feature/customer/data/Booking.kt",
    "data/model/RepairRequest.kt": "feature/customer/data/RepairRequest.kt",
    "data/model/Quote.kt": "feature/customer/data/Quote.kt",
    "data/model/Service.kt": "feature/customer/data/Service.kt",
    "data/model/ServiceCategory.kt": "feature/customer/data/ServiceCategory.kt",
    
    "domain/usecase/booking/CreateBookingUseCase.kt": "feature/customer/domain/usecase/CreateBookingUseCase.kt",

    "presentation/customer/ActiveRepairScreen.kt": "feature/customer/presentation/ActiveRepairScreen.kt",
    "presentation/customer/ActiveRepairViewModel.kt": "feature/customer/presentation/ActiveRepairViewModel.kt",
    "presentation/customer/BookingDetailScreen.kt": "feature/customer/presentation/BookingDetailScreen.kt",
    "presentation/customer/BookingDetailViewModel.kt": "feature/customer/presentation/BookingDetailViewModel.kt",
    "presentation/customer/BookingsListContent.kt": "feature/customer/presentation/BookingsListContent.kt",
    "presentation/customer/CreateBookingScreen.kt": "feature/customer/presentation/CreateBookingScreen.kt",
    "presentation/customer/CreateBookingViewModel.kt": "feature/customer/presentation/CreateBookingViewModel.kt",
    "presentation/customer/CustomerDashboardScreen.kt": "feature/customer/presentation/CustomerDashboardScreen.kt",
    "presentation/customer/CustomerDashboardViewModel.kt": "feature/customer/presentation/CustomerDashboardViewModel.kt",
    "presentation/customer/DiagnosisResultCard.kt": "feature/customer/presentation/DiagnosisResultCard.kt",
    "presentation/customer/LocationMapScreen.kt": "feature/customer/presentation/LocationMapScreen.kt",
    "presentation/customer/PostRepairRequestScreen.kt": "feature/customer/presentation/PostRepairRequestScreen.kt",
    "presentation/customer/PostRepairRequestViewModel.kt": "feature/customer/presentation/PostRepairRequestViewModel.kt",
    "presentation/customer/QuoteManagementScreen.kt": "feature/customer/presentation/QuoteManagementScreen.kt",
    "presentation/customer/QuoteManagementViewModel.kt": "feature/customer/presentation/QuoteManagementViewModel.kt",
    "presentation/home/HomeScreen.kt": "feature/customer/presentation/HomeScreen.kt",

    # Vendor feature
    "repository/VendorRepository.kt": "feature/vendor/data/VendorRepository.kt",
    "data/model/Vendor.kt": "feature/vendor/domain/Vendor.kt",
    "domain/usecase/vendor/GetMatchedVendorsUseCase.kt": "feature/vendor/domain/usecase/GetMatchedVendorsUseCase.kt",
    
    "presentation/vendor/RepairFeedScreen.kt": "feature/vendor/presentation/RepairFeedScreen.kt",
    "presentation/vendor/SubmitQuoteScreen.kt": "feature/vendor/presentation/SubmitQuoteScreen.kt",
    "presentation/vendor/VendorDashboardScreen.kt": "feature/vendor/presentation/VendorDashboardScreen.kt",
    "presentation/vendor/MyJobsContent.kt": "feature/vendor/presentation/MyJobsContent.kt",
    "presentation/vendor/MyJobsViewModel.kt": "feature/vendor/presentation/MyJobsViewModel.kt",
    "presentation/vendor/RepairFeedViewModel.kt": "feature/vendor/presentation/RepairFeedViewModel.kt",
    "presentation/vendor/VendorDashboardViewModel.kt": "feature/vendor/presentation/VendorDashboardViewModel.kt",

    # Marketplace feature
    "repository/marketplace/MarketplaceRepositoryImpl.kt": "feature/marketplace/data/MarketplaceRepositoryImpl.kt",
    "domain/repository/MarketplaceRepository.kt": "feature/marketplace/domain/MarketplaceRepository.kt",
    "domain/model/marketplace/BookingState.kt": "feature/marketplace/domain/BookingState.kt",
    "domain/model/marketplace/Quote.kt": "feature/marketplace/domain/Quote.kt",
    "domain/model/marketplace/RepairRequest.kt": "feature/marketplace/domain/RepairRequest.kt",
    
    "presentation/marketplace/ui/ActiveBiddingScreen.kt": "feature/marketplace/presentation/ActiveBiddingScreen.kt",
    "presentation/marketplace/ui/MarketplaceScreen.kt": "feature/marketplace/presentation/MarketplaceScreen.kt",
    "presentation/marketplace/ui/PlaceholderScreens.kt": "feature/marketplace/presentation/PlaceholderScreens.kt",
    "presentation/marketplace/ui/TerminalStateScreens.kt": "feature/marketplace/presentation/TerminalStateScreens.kt",
    "presentation/marketplace/MarketplaceViewModel.kt": "feature/marketplace/presentation/MarketplaceViewModel.kt"
}

def to_package(path):
    if not path: return "com.servify.app"
    clean_path = path.replace("\\", "/").rsplit("/", 1)[0]
    if clean_path.endswith(".kt"):
        return "com.servify.app"
    return "com.servify.app." + clean_path.replace("/", ".")

def get_class_name(path):
    return os.path.basename(path).replace(".kt", "")

def get_all_kt_files():
    kt_dirs = []
    for root, _, files in os.walk(BASE_DIR):
        for f in files:
            if f.endswith(".kt"):
                kt_dirs.append(os.path.join(root, f))
    return kt_dirs

print("Starting Forward-Only Screaming Architecture Refactoring...")

# 1. Map old imports to new imports
import_replacements = {}

for old_rel, new_rel in MOVES.items():
    old_pkg = to_package(old_rel)
    new_pkg = to_package(new_rel)
    cls_name = get_class_name(old_rel)
    
    old_import = f"{old_pkg}.{cls_name}"
    new_import = f"{new_pkg}.{cls_name}"
    
    if old_pkg != new_pkg:
        import_replacements[old_import] = new_import

# 2. Perform the Moves
moved_count = 0
for old_rel, new_rel in MOVES.items():
    old_abs = os.path.join(BASE_DIR, os.path.normpath(old_rel))
    new_abs = os.path.join(BASE_DIR, os.path.normpath(new_rel))
    
    if not os.path.exists(old_abs):
        alt_abs = os.path.join(BASE_DIR, "data", os.path.normpath(old_rel))
        if os.path.exists(alt_abs):
            old_abs = alt_abs
        else:
            continue

    if old_abs == new_abs:
        continue
        
    os.makedirs(os.path.dirname(new_abs), exist_ok=True)
    shutil.move(old_abs, new_abs)
    moved_count += 1
    
    # Update package declaration
    old_pkg = to_package(old_rel)
    if "repository" in old_rel and not old_rel.startswith("data/"):
        old_pkg = to_package("data/" + old_rel)
        
    new_pkg = to_package(new_rel)
    
    with open(new_abs, 'r', encoding='utf-8') as f:
        content = f.read()
        
    content = re.sub(r"^package\s+com\.servify\.app.*$", f"package {new_pkg}", content, flags=re.MULTILINE)
    
    with open(new_abs, 'w', encoding='utf-8') as f:
        f.write(content)

print(f"Successfully moved {moved_count} files and updated package declarations.")

# 3. Update all imports across the codebase (best effort regex replace)
files_updated = 0
for kt_path in get_all_kt_files():
    with open(kt_path, 'r', encoding='utf-8') as f:
        data = f.read()
        
    original_data = data
    for old_imp, new_imp in import_replacements.items():
        data = data.replace(f"import {old_imp}", f"import {new_imp}")
        
    if data != original_data:
        with open(kt_path, 'w', encoding='utf-8') as f:
            f.write(data)
        files_updated += 1
        
print(f"Updated imports in {files_updated} files.")
print("DONE. Please run git add and commit.")
