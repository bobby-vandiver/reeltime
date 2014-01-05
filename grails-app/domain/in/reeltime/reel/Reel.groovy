package in.reeltime.reel

class Reel {

    static hasOne = [audience: Audience]

    static constraints = {
        audience unique: true
    }
}
