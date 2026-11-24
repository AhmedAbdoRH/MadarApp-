"""Offline source regression checks, not Android UI or backend integration tests.
Run from repository root: python3 local-tests/ux_source_checks.py
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / 'app/src/main/java/com/example'
checks = 0

def read(path):
    return (SRC / path).read_text()

def verify(condition, label):
    global checks
    assert condition, label
    checks += 1

vm = read('ui/SanadViewModel.kt')
booking = vm[vm.index('    fun confirmBooking('):vm.index('    fun requestBookingCancellation(')]
cancel = vm[vm.index('    fun confirmBookingCancellation('):vm.index('    fun setEmergencyDialogOpen(')]
for section, remote in [(booking, 'firestoreRepo.bookSlot('), (cancel, 'firestoreRepo.cancelBooking(')]:
    verify(section.index(remote) < section.index('showSnackbar("تم'), 'remote success precedes success feedback')
    verify('SanadRepository.' not in section, 'no optimistic local booking mutation')
    verify('isBookingBusy) return' in section, 'duplicate-submit guard')
    verify('CancellationException' in section and 'throw e' in section, 'structured cancellation propagated')
    verify('finally' in section and 'isBookingBusy = false' in section, 'busy state released')
    verify('لم يتأكد' in section, 'failure is visible without claiming confirmed outcome')
main = read('MainActivity.kt')
verify(main.count('viewModel.requestBookingCancellation(slotId)') == 2, 'both seeker and specialist request confirmation')
verify('viewModel.cancelBooking(' not in main, 'no direct cancellation callback')
verify('viewModel.confirmBookingCancellation()' in main, 'explicit confirmation callback')
verify('احتفظ بالحجز' in main, 'non-destructive dismissal')
consent = read('ui/screens/WelcomeConsentScreen.kt')
verify('mutableStateOf(false)' in consent and 'adult_consent_checkbox' in consent, 'consent starts unchecked')
verify('(selectedAgeOption == AgeOption.ADULT && adultConsent)' in consent, 'adult route requires separate consent')
verify('عرض موارد المساعدة' in consent, 'distinct under-18 label')
mood = read('ui/screens/WellbeingScreen.kt')
verify('mutableStateOf<Int?>(null)' in mood, 'no default mood')
verify('enabled = selectedScore != null' in mood, 'mood submission gated')
verify('?: moodOptions[2]' not in mood, 'no fallback implicit mood')
verify('selectedScore = null' in mood, 'mood resets after callback')
urgent = read('ui/screens/UrgentAssistanceScreen.kt')
verify('فوري وسري' not in urgent, 'unsupported secrecy and timing removed')
privacy = read('ui/screens/PrivacyPolicyScreen.kt')
verify('السياسة غير مكتملة' in privacy, 'policy incompleteness explicit')
verify('verticalScroll' in privacy, 'policy scrolls for larger font settings')
print(f'PASS: {checks} offline source checks. No app, accounts, or network exercised.')
