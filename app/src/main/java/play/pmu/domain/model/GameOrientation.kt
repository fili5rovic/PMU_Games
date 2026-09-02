package play.pmu.domain.model

/**
 * Orijentacija u kojoj se igra igra.
 *
 * Orijentacija je u ovoj aplikaciji deo pravila: igre na podeljenom ekranu
 * zahtevaju portret (telefon lezi izmedju igraca), a pantomima landscape (telefon
 * se drzi polozeno na celu). Zato je nosi sama igra, a ne ekran - vidi
 * LockScreenOrientation, jedino mesto koje je primenjuje.
 */
enum class GameOrientation { PORTRAIT, LANDSCAPE }
