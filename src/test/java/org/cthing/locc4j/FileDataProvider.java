/*
 * Copyright 2024 C Thing Software
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cthing.locc4j;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;

import static org.cthing.locc4j.Language.ABNF;
import static org.cthing.locc4j.Language.AWK;
import static org.cthing.locc4j.Language.Abap;
import static org.cthing.locc4j.Language.ActionScript;
import static org.cthing.locc4j.Language.Ada;
import static org.cthing.locc4j.Language.Agda;
import static org.cthing.locc4j.Language.Alex;
import static org.cthing.locc4j.Language.Alloy;
import static org.cthing.locc4j.Language.Apl;
import static org.cthing.locc4j.Language.Arduino;
import static org.cthing.locc4j.Language.AsciiDoc;
import static org.cthing.locc4j.Language.Asn1;
import static org.cthing.locc4j.Language.Asp;
import static org.cthing.locc4j.Language.AspNet;
import static org.cthing.locc4j.Language.Assembly;
import static org.cthing.locc4j.Language.AssemblyGAS;
import static org.cthing.locc4j.Language.Astro;
import static org.cthing.locc4j.Language.Ats;
import static org.cthing.locc4j.Language.AutoHotKey;
import static org.cthing.locc4j.Language.Autoconf;
import static org.cthing.locc4j.Language.Autoit;
import static org.cthing.locc4j.Language.Automake;
import static org.cthing.locc4j.Language.Avalonia;
import static org.cthing.locc4j.Language.Bash;
import static org.cthing.locc4j.Language.Batch;
import static org.cthing.locc4j.Language.Bazel;
import static org.cthing.locc4j.Language.Bean;
import static org.cthing.locc4j.Language.Bitbake;
import static org.cthing.locc4j.Language.BrightScript;
import static org.cthing.locc4j.Language.C;
import static org.cthing.locc4j.Language.CHeader;
import static org.cthing.locc4j.Language.CMake;
import static org.cthing.locc4j.Language.CSharp;
import static org.cthing.locc4j.Language.CShell;
import static org.cthing.locc4j.Language.Cabal;
import static org.cthing.locc4j.Language.Cassius;
import static org.cthing.locc4j.Language.Ceylon;
import static org.cthing.locc4j.Language.Clojure;
import static org.cthing.locc4j.Language.ClojureC;
import static org.cthing.locc4j.Language.ClojureScript;
import static org.cthing.locc4j.Language.Cobol;
import static org.cthing.locc4j.Language.CodeQL;
import static org.cthing.locc4j.Language.CoffeeScript;
import static org.cthing.locc4j.Language.Cogent;
import static org.cthing.locc4j.Language.ColdFusion;
import static org.cthing.locc4j.Language.ColdFusionScript;
import static org.cthing.locc4j.Language.Coq;
import static org.cthing.locc4j.Language.Cpp;
import static org.cthing.locc4j.Language.CppHeader;
import static org.cthing.locc4j.Language.Crystal;
import static org.cthing.locc4j.Language.Css;
import static org.cthing.locc4j.Language.Cuda;
import static org.cthing.locc4j.Language.Cython;
import static org.cthing.locc4j.Language.D;
import static org.cthing.locc4j.Language.DTD;
import static org.cthing.locc4j.Language.Daml;
import static org.cthing.locc4j.Language.Dart;
import static org.cthing.locc4j.Language.DeviceTree;
import static org.cthing.locc4j.Language.Dhall;
import static org.cthing.locc4j.Language.Dockerfile;
import static org.cthing.locc4j.Language.DotNetResource;
import static org.cthing.locc4j.Language.DreamMaker;
import static org.cthing.locc4j.Language.Dsssl;
import static org.cthing.locc4j.Language.Dust;
import static org.cthing.locc4j.Language.ESDL;
import static org.cthing.locc4j.Language.Ebuild;
import static org.cthing.locc4j.Language.EdgeQL;
import static org.cthing.locc4j.Language.Edn;
import static org.cthing.locc4j.Language.Elisp;
import static org.cthing.locc4j.Language.Elixir;
import static org.cthing.locc4j.Language.Elm;
import static org.cthing.locc4j.Language.Elvish;
import static org.cthing.locc4j.Language.EmacsDevEnv;
import static org.cthing.locc4j.Language.Emojicode;
import static org.cthing.locc4j.Language.Erlang;
import static org.cthing.locc4j.Language.Expect;
import static org.cthing.locc4j.Language.FEN;
import static org.cthing.locc4j.Language.FSharp;
import static org.cthing.locc4j.Language.Factor;
import static org.cthing.locc4j.Language.Fennel;
import static org.cthing.locc4j.Language.Fish;
import static org.cthing.locc4j.Language.FlatBuffers;
import static org.cthing.locc4j.Language.ForgeConfig;
import static org.cthing.locc4j.Language.Forth;
import static org.cthing.locc4j.Language.FortranLegacy;
import static org.cthing.locc4j.Language.FortranModern;
import static org.cthing.locc4j.Language.FreeMarker;
import static org.cthing.locc4j.Language.Fstar;
import static org.cthing.locc4j.Language.Futhark;
import static org.cthing.locc4j.Language.GDB;
import static org.cthing.locc4j.Language.GdScript;
import static org.cthing.locc4j.Language.Gherkin;
import static org.cthing.locc4j.Language.Gleam;
import static org.cthing.locc4j.Language.Glsl;
import static org.cthing.locc4j.Language.Gml;
import static org.cthing.locc4j.Language.Go;
import static org.cthing.locc4j.Language.Gohtml;
import static org.cthing.locc4j.Language.GradleGroovy;
import static org.cthing.locc4j.Language.GradleKotlin;
import static org.cthing.locc4j.Language.Graphql;
import static org.cthing.locc4j.Language.Groovy;
import static org.cthing.locc4j.Language.Gwion;
import static org.cthing.locc4j.Language.Haml;
import static org.cthing.locc4j.Language.Hamlet;
import static org.cthing.locc4j.Language.Handlebars;
import static org.cthing.locc4j.Language.Haskell;
import static org.cthing.locc4j.Language.Haxe;
import static org.cthing.locc4j.Language.Hcl;
import static org.cthing.locc4j.Language.Headache;
import static org.cthing.locc4j.Language.HiCad;
import static org.cthing.locc4j.Language.Hlsl;
import static org.cthing.locc4j.Language.HolyC;
import static org.cthing.locc4j.Language.Html;
import static org.cthing.locc4j.Language.Hy;
import static org.cthing.locc4j.Language.Idris;
import static org.cthing.locc4j.Language.Ini;
import static org.cthing.locc4j.Language.IntelHex;
import static org.cthing.locc4j.Language.Isabelle;
import static org.cthing.locc4j.Language.Jai;
import static org.cthing.locc4j.Language.Java;
import static org.cthing.locc4j.Language.JavaProperties;
import static org.cthing.locc4j.Language.JavaScript;
import static org.cthing.locc4j.Language.Jinja2;
import static org.cthing.locc4j.Language.Jq;
import static org.cthing.locc4j.Language.Json;
import static org.cthing.locc4j.Language.Jsonnet;
import static org.cthing.locc4j.Language.Jsx;
import static org.cthing.locc4j.Language.Julia;
import static org.cthing.locc4j.Language.Julius;
import static org.cthing.locc4j.Language.Jupyter;
import static org.cthing.locc4j.Language.K;
import static org.cthing.locc4j.Language.KakouneScript;
import static org.cthing.locc4j.Language.Kotlin;
import static org.cthing.locc4j.Language.Ksh;
import static org.cthing.locc4j.Language.KvLanguage;
import static org.cthing.locc4j.Language.LLVM;
import static org.cthing.locc4j.Language.Lean;
import static org.cthing.locc4j.Language.Less;
import static org.cthing.locc4j.Language.LinkerScript;
import static org.cthing.locc4j.Language.Liquid;
import static org.cthing.locc4j.Language.Lisp;
import static org.cthing.locc4j.Language.LiveScript;
import static org.cthing.locc4j.Language.Logtalk;
import static org.cthing.locc4j.Language.LolCode;
import static org.cthing.locc4j.Language.Lua;
import static org.cthing.locc4j.Language.Lucius;
import static org.cthing.locc4j.Language.M4;
import static org.cthing.locc4j.Language.Madlang;
import static org.cthing.locc4j.Language.Makefile;
import static org.cthing.locc4j.Language.Markdown;
import static org.cthing.locc4j.Language.Mermaid;
import static org.cthing.locc4j.Language.Meson;
import static org.cthing.locc4j.Language.Metal;
import static org.cthing.locc4j.Language.Mint;
import static org.cthing.locc4j.Language.Mlatu;
import static org.cthing.locc4j.Language.ModuleDef;
import static org.cthing.locc4j.Language.MoonScript;
import static org.cthing.locc4j.Language.MsBuild;
import static org.cthing.locc4j.Language.Mustache;
import static org.cthing.locc4j.Language.Nextflow;
import static org.cthing.locc4j.Language.Nim;
import static org.cthing.locc4j.Language.Nix;
import static org.cthing.locc4j.Language.NotQuitePerl;
import static org.cthing.locc4j.Language.NuGetConfig;
import static org.cthing.locc4j.Language.Nushell;
import static org.cthing.locc4j.Language.OCaml;
import static org.cthing.locc4j.Language.ObjectiveC;
import static org.cthing.locc4j.Language.ObjectiveCpp;
import static org.cthing.locc4j.Language.Odin;
import static org.cthing.locc4j.Language.OpenPolicyAgent;
import static org.cthing.locc4j.Language.OpenType;
import static org.cthing.locc4j.Language.Org;
import static org.cthing.locc4j.Language.Oz;
import static org.cthing.locc4j.Language.POM;
import static org.cthing.locc4j.Language.PSL;
import static org.cthing.locc4j.Language.PacmanMakepkg;
import static org.cthing.locc4j.Language.Pan;
import static org.cthing.locc4j.Language.Pascal;
import static org.cthing.locc4j.Language.Perl;
import static org.cthing.locc4j.Language.Pest;
import static org.cthing.locc4j.Language.Php;
import static org.cthing.locc4j.Language.Poke;
import static org.cthing.locc4j.Language.Pony;
import static org.cthing.locc4j.Language.PostCss;
import static org.cthing.locc4j.Language.PowerShell;
import static org.cthing.locc4j.Language.Processing;
import static org.cthing.locc4j.Language.Prolog;
import static org.cthing.locc4j.Language.Protobuf;
import static org.cthing.locc4j.Language.Pug;
import static org.cthing.locc4j.Language.Puppet;
import static org.cthing.locc4j.Language.PureScript;
import static org.cthing.locc4j.Language.Python;
import static org.cthing.locc4j.Language.Q;
import static org.cthing.locc4j.Language.Qcl;
import static org.cthing.locc4j.Language.Qml;
import static org.cthing.locc4j.Language.R;
import static org.cthing.locc4j.Language.RON;
import static org.cthing.locc4j.Language.RPMSpecfile;
import static org.cthing.locc4j.Language.Racket;
import static org.cthing.locc4j.Language.Rakefile;
import static org.cthing.locc4j.Language.Raku;
import static org.cthing.locc4j.Language.Razor;
import static org.cthing.locc4j.Language.ReScript;
import static org.cthing.locc4j.Language.ReStructuredText;
import static org.cthing.locc4j.Language.Redscript;
import static org.cthing.locc4j.Language.Renpy;
import static org.cthing.locc4j.Language.Ruby;
import static org.cthing.locc4j.Language.RubyHtml;
import static org.cthing.locc4j.Language.Rust;
import static org.cthing.locc4j.Language.SRecode;
import static org.cthing.locc4j.Language.Sass;
import static org.cthing.locc4j.Language.Scala;
import static org.cthing.locc4j.Language.Scheme;
import static org.cthing.locc4j.Language.Scons;
import static org.cthing.locc4j.Language.Sed;
import static org.cthing.locc4j.Language.Sgml;
import static org.cthing.locc4j.Language.Sh;
import static org.cthing.locc4j.Language.ShaderLab;
import static org.cthing.locc4j.Language.Slint;
import static org.cthing.locc4j.Language.Smalltalk;
import static org.cthing.locc4j.Language.Sml;
import static org.cthing.locc4j.Language.Solidity;
import static org.cthing.locc4j.Language.SpecmanE;
import static org.cthing.locc4j.Language.Spice;
import static org.cthing.locc4j.Language.Sqf;
import static org.cthing.locc4j.Language.Sql;
import static org.cthing.locc4j.Language.Stan;
import static org.cthing.locc4j.Language.Stratego;
import static org.cthing.locc4j.Language.Stylus;
import static org.cthing.locc4j.Language.Svelte;
import static org.cthing.locc4j.Language.Svg;
import static org.cthing.locc4j.Language.Swift;
import static org.cthing.locc4j.Language.Swig;
import static org.cthing.locc4j.Language.SystemVerilog;
import static org.cthing.locc4j.Language.Tcl;
import static org.cthing.locc4j.Language.Tera;
import static org.cthing.locc4j.Language.Tex;
import static org.cthing.locc4j.Language.Text;
import static org.cthing.locc4j.Language.Thrift;
import static org.cthing.locc4j.Language.Toml;
import static org.cthing.locc4j.Language.Tsx;
import static org.cthing.locc4j.Language.Ttcn;
import static org.cthing.locc4j.Language.Twig;
import static org.cthing.locc4j.Language.TypeScript;
import static org.cthing.locc4j.Language.UMPL;
import static org.cthing.locc4j.Language.Unison;
import static org.cthing.locc4j.Language.UnrealDeveloperMarkdown;
import static org.cthing.locc4j.Language.UnrealPlugin;
import static org.cthing.locc4j.Language.UnrealProject;
import static org.cthing.locc4j.Language.UnrealScript;
import static org.cthing.locc4j.Language.UnrealShader;
import static org.cthing.locc4j.Language.UnrealShaderHeader;
import static org.cthing.locc4j.Language.UrWeb;
import static org.cthing.locc4j.Language.UrWebProject;
import static org.cthing.locc4j.Language.VB6;
import static org.cthing.locc4j.Language.VBScript;
import static org.cthing.locc4j.Language.Vala;
import static org.cthing.locc4j.Language.Velocity;
import static org.cthing.locc4j.Language.Verilog;
import static org.cthing.locc4j.Language.VerilogArgsFile;
import static org.cthing.locc4j.Language.Vhdl;
import static org.cthing.locc4j.Language.VisualBasic;
import static org.cthing.locc4j.Language.VisualStudioProject;
import static org.cthing.locc4j.Language.VisualStudioSolution;
import static org.cthing.locc4j.Language.VoiceXml;
import static org.cthing.locc4j.Language.Vue;
import static org.cthing.locc4j.Language.WGSL;
import static org.cthing.locc4j.Language.WebAssembly;
import static org.cthing.locc4j.Language.WenYan;
import static org.cthing.locc4j.Language.Wolfram;
import static org.cthing.locc4j.Language.XSL;
import static org.cthing.locc4j.Language.XSLFO;
import static org.cthing.locc4j.Language.Xaml;
import static org.cthing.locc4j.Language.XcodeConfig;
import static org.cthing.locc4j.Language.Xml;
import static org.cthing.locc4j.Language.Xtend;
import static org.cthing.locc4j.Language.Yaml;
import static org.cthing.locc4j.Language.ZenCode;
import static org.cthing.locc4j.Language.Zig;
import static org.cthing.locc4j.Language.Zsh;
import static org.junit.jupiter.params.provider.Arguments.arguments;


/**
 * Provides the test files and their expected counts for unit testing.
 */
public class FileDataProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(final ParameterDeclarations paramDecls,
                                                        final ExtensionContext context) {
        return Stream.of(
                arguments("abap.abap", true, Abap, 67, 3, 12),
                arguments("abnf.abnf", true, ABNF, 3, 4, 3),
                arguments("actionscript.as", true, ActionScript, 56, 4, 10),
                arguments("ada.ada", true, Ada, 14, 15, 5),
                arguments("agda.agda", true, Agda, 35, 62, 28),
                arguments("alex.x", true, Alex, 27, 7, 9),
                arguments("alloy.als", true, Alloy, 10, 2, 5),
                arguments("apl.apl", true, Apl, 11, 1, 0),
                arguments("arduino.ino", true, Arduino, 13, 5, 4),
                arguments("asciidoc.adoc", true, AsciiDoc, 5, 7, 6),
                arguments("asm.asm", true, Assembly, 22, 3, 4),
                arguments("asn1.asn1", true, Asn1, 16, 10, 7),
                arguments("snmp.mib", true, Asn1, 229, 15, 26),
                arguments("asp.asp", true, Asp, 64, 0, 7),
                arguments("aspnet.aspx", true, AspNet, 24, 4, 12),
                arguments("astro.astro", true, Astro, 44, 9, 8),
                arguments("atlocal.in", true, Autoconf, 20, 25, 7),
                arguments("ats.dats", true, Ats, 25, 8, 7),
                arguments("autohotkey.ahk", true, AutoHotKey, 83, 28, 35),
                arguments("autoit.au3", true, Autoit, 19, 25, 12),
                arguments("automake.am", true, Automake, 69, 4, 19),
                arguments("avalonia.axaml", true, Avalonia, 74, 4, 4),
                arguments("awk.awk", true, AWK, 1, 2, 1),
                arguments("shell.bash", true, Bash, 6, 2, 2),
                arguments("batch.bat", true, Batch, 32, 10, 8),
                arguments("bazel.bzl", true, Bazel, 13, 2, 1),
                arguments("bean.bean", true, Bean, 13, 5, 7),
                arguments("bitbake.bb", true, Bitbake, 13, 4, 5),
                arguments("brightscript.brs", true, BrightScript, 10, 12, 3),
                arguments("c.c", true, C, 33, 7, 8),
                arguments("cabal.cabal", true, Cabal, 66, 5, 11),
                arguments("cassius.cassius", true, Cassius, 6, 1, 0),
                arguments("ceylon.ceylon", true, Ceylon, 170, 14, 15),
                arguments("c.h", true, CHeader, 4, 4, 3),
                arguments("spice.ckt", true, Spice, 51, 8, 4),
                arguments("clojure.clj", true, Clojure, 13, 2, 2),
                arguments("clojurec.cljc", true, ClojureC, 13, 2, 2),
                arguments("clojurescript.cljs", true, ClojureScript, 13, 2, 2),
                arguments("cmake.cmake", true, CMake, 16, 2, 5),
                arguments("cobol.cbl", true, Cobol, 139, 71, 41),
                arguments("codeql.ql", true, CodeQL, 17, 14, 8),
                arguments("coffeescript.coffee", true, CoffeeScript, 13, 8, 7),
                arguments("cogent.cogent", true, Cogent, 2, 1, 2),
                arguments("coldfusion.cfm", true, ColdFusion, 50, 6, 9),
                arguments("coldfusionscript.cfc", true, ColdFusionScript, 45, 5, 7),
                arguments("coq.v", true, Coq, 164, 191, 123),
                arguments("cpp.cpp", true, Cpp, 37, 2, 6),
                arguments("hpp.hpp", true, CppHeader, 11, 4, 5),
                arguments("crystal.cr", true, Crystal, 14, 1, 4),
                arguments("shell.csh", true, CShell, 6, 2, 2),
                arguments("csharp.cs", true, CSharp, 14, 8, 3),
                arguments("css.css", true, Css, 75, 27, 15),
                arguments("cuda.cu", true, Cuda, 4, 1, 0),
                arguments("cython.pyx", true, Cython, 9, 14, 3),
                arguments("cython.pyx", false, Cython, 21, 2, 3),
                arguments("d.d", true, D, 5, 1, 2),
                arguments("Daml.daml", true, Daml, 24, 8, 8),
                arguments("dart.dart", true, Dart, 29, 25, 11),
                arguments("devicetree.dts", true, DeviceTree, 12, 4, 1),
                arguments("dhall.dhall", true, Dhall, 9, 4, 2),
                arguments("Dockerfile", true, Dockerfile, 7, 2, 7),
                arguments("resource.resx", true, DotNetResource, 10, 1, 1),
                arguments("dreammaker.dm", true, DreamMaker, 7, 5, 4),
                arguments("dsssl.dsl", true, Dsssl, 25, 2, 5),
                arguments("dtd.dtd", true, DTD, 21, 12, 3),
                arguments("dust.dust", true, Dust, 2, 4, 2),
                arguments("ebuild.ebuild", true, Ebuild, 9, 1, 4),
                arguments("edgeql.edgeql", true, EdgeQL, 21, 2, 4),
                arguments("edn.edn", true, Edn, 6, 1, 2),
                arguments("elixir.ex", true, Elixir, 43, 17, 9),
                arguments("elm.elm", true, Elm, 106, 13, 50),
                arguments("elvish.elv", true, Elvish, 9, 1, 5),
                arguments("emacs_dev_env.ede", true, EmacsDevEnv, 6, 6, 2),
                arguments("emacs_lisp.el", true, Elisp, 11, 5, 4),
                arguments("emojicode.emojic", true, Emojicode, 10, 10, 4),
                arguments("erlang.erl", true, Erlang, 91, 308, 48),
                arguments("esdl.esdl", true, ESDL, 13, 3, 2),
                arguments("expect.exp", true, Expect, 20, 5, 7),
                arguments("factor.factor", true, Factor, 5, 5, 3),
                arguments("fen.fen", true, FEN, 4, 0, 0),
                arguments("fennel.fnl", true, Fennel, 8, 4, 4),
                arguments("fish.fish", true, Fish, 9, 10, 3),
                arguments("flatbuffers.fbs", true, FlatBuffers, 21, 5, 7),
                arguments("forgecfg.cfg", true, ForgeConfig, 20, 39, 18),
                arguments("forth.frt", true, Forth, 19, 6, 7),
                arguments("fortran_old.for", true, FortranLegacy, 18, 4, 0),
                arguments("fortran_new.f90", true, FortranModern, 68, 33, 21),
                arguments("fsharp.fs", true, FSharp, 6, 4, 4),
                arguments("fstar.fst", true, Fstar, 3, 4, 3),
                arguments("ftl.ftl", true, FreeMarker, 5, 2, 2),
                arguments("futhark.fut", true, Futhark, 2, 2, 3),
                arguments("gas.S", true, AssemblyGAS, 46, 9, 11),
                arguments("gdb.gdb", true, GDB, 7, 4, 3),
                arguments("gdscript.gd", true, GdScript, 110, 2, 21),
                arguments("gherkin.feature", true, Gherkin, 8, 2, 2),
                arguments("gleam.gleam", true, Gleam, 24, 3, 5),
                arguments("glsl.glsl", true, Glsl, 36, 4, 5),
                arguments("gml.gml", true, Gml, 5, 8, 3),
                arguments("go.go", true, Go, 24, 5, 6),
                arguments("gohtml.gohtml", true, Gohtml, 20, 13, 7),
                arguments("build.gradle", true, GradleGroovy, 17, 10, 6),
                arguments("build.gradle.kts", true, GradleKotlin, 195, 4, 35),
                arguments("graphql.gql", true, Graphql, 71, 2, 14),
                arguments("groovy.groovy", true, Groovy, 24, 3, 2),
                arguments("gwion.gw", true, Gwion, 8, 2, 2),
                arguments("haml.haml", true, Haml, 11, 1, 4),
                arguments("hamlet.hamlet", true, Hamlet, 15, 1, 0),
                arguments("handlebars.hbs", true, Handlebars, 76, 6, 2),
                arguments("haskell.hs", true, Haskell, 109, 54, 33),
                arguments("haxe.hx", true, Haxe, 94, 55, 44),
                arguments("hcl.tf", true, Hcl, 11, 6, 4),
                arguments("headache.ha", true, Headache, 9, 2, 1),
                arguments("hicad.mac", true, HiCad, 4, 3, 3),
                arguments("hlsl.hlsl", true, Hlsl, 30, 10, 5),
                arguments("holyc.hc", true, HolyC, 101, 5, 17),
                arguments("html.html", true, Html, 19, 7, 4, Css, 3, 5, 0, JavaScript, 1, 6, 0),
                arguments("hy.hy", true, Hy, 44, 3, 14),
                arguments("idris.idr", true, Idris, 14, 6, 5),
                arguments("ini.ini", true, Ini, 7, 2, 1),
                arguments("intel.hex", true, IntelHex, 5, 0, 0),
                arguments("isabelle.thy", true, Isabelle, 82, 18, 24),
                arguments("jai.jai", true, Jai, 37, 7, 7),
                arguments("java.java", true, Java, 26, 9, 9),
                arguments("javascript.js", true, JavaScript, 14, 11, 6),
                arguments("jinja2.j2", true, Jinja2, 1, 1, 2),
                arguments("jq.jq", true, Jq, 3, 4, 2),
                arguments("json.json", true, Json, 3167, 0, 1),
                arguments("jsonnet.jsonnet", true, Jsonnet, 7, 3, 1),
                arguments("jsx.jsx", true, Jsx, 17, 4, 1),
                arguments("julia.jl", true, Julia, 21, 15, 17),
                arguments("julius.julius", true, Julius, 6, 1, 1),
                arguments("jupyter.ipynb", true, Jupyter, 2191, 0, 0, Markdown, 113, 0, 10, Python, 528, 220, 105),
                arguments("k.k", true, K, 2, 3, 2),
                arguments("kakoune_script.kak", true, KakouneScript, 8, 1, 3),
                arguments("kotlin.kt", true, Kotlin, 101, 6, 23),
                arguments("ksh.ksh", true, Ksh, 11, 3, 2),
                arguments("kvlanguage.kv", true, KvLanguage, 17, 2, 2),
                arguments("lean.lean", true, Lean, 81, 17, 21),
                arguments("less.less", true, Less, 24, 4, 2),
                arguments("linkerscript.ld", true, LinkerScript, 8, 3, 0),
                arguments("liquid.liquid", true, Liquid, 21, 1, 5, JavaScript, 1, 1, 0),
                arguments("lisp.lisp", true, Lisp, 126, 17, 31),
                arguments("livescript.ls", true, LiveScript, 10, 11, 5),
                arguments("llvm.ll", true, LLVM, 17, 1, 3),
                arguments("logtalk.lgt", true, Logtalk, 27, 16, 20),
                arguments("lolcode.lol", true, LolCode, 11, 9, 6),
                arguments("lua.lua", true, Lua, 24, 3, 10),
                arguments("lucius.lucius", true, Lucius, 8, 1, 1),
                arguments("m4.m4", true, M4, 3, 3, 1),
                arguments("madlang.mad", true, Madlang, 39, 5, 8),
                arguments("Makefile", true, Makefile, 11, 4, 7),
                arguments("markdown.md", true, Markdown, 8, 0, 3, Mermaid, 12, 2, 1),
                arguments("mermaid.mmd", true, Mermaid, 15, 2, 1),
                arguments("meson.build", true, Meson, 6, 1, 3),
                arguments("metal.metal", true, Metal, 21, 4, 6),
                arguments("mint.mint", true, Mint, 22, 6, 2),
                arguments("mlatu.mlt", true, Mlatu, 14, 3, 5),
                arguments("moduledef.def", true, ModuleDef, 9, 5, 2),
                arguments("moonscript.moon", true, MoonScript, 59, 1, 22),
                arguments("MSBuild.csproj", true, MsBuild, 10, 1, 2),
                arguments("mustache.mustache", true, Mustache, 26, 3, 3),
                arguments("nextflow.nf", true, Nextflow, 10, 4, 2),
                arguments("nim.nim", true, Nim, 73, 22, 33),
                arguments("nix.nix", true, Nix, 110, 4, 26),
                arguments("nqp.nqp", true, NotQuitePerl, 14, 8, 2),
                arguments("NuGet.Config", true, NuGetConfig, 13, 7, 2),
                arguments("nushell.nu", true, Nushell, 20, 29, 14),
                arguments("objectivec.m", true, ObjectiveC, 32, 8, 13),
                arguments("objectivecpp.mm", true, ObjectiveCpp, 20, 9, 10),
                arguments("ocaml.ml", true, OCaml, 187, 150, 100),
                arguments("odin.odin", true, Odin, 17, 7, 5),
                arguments("open_policy_agent.rego", true, OpenPolicyAgent, 8, 2, 2),
                arguments("opentype.fea", true, OpenType, 24, 23, 6),
                arguments("org_mode.org", true, Org, 7, 1, 3),
                arguments("mozart.oz", true, Oz, 41, 44, 16),
                arguments("PKGBUILD", true, PacmanMakepkg, 19, 2, 2),
                arguments("pan.pan", true, Pan, 11, 3, 6),
                arguments("pascal.pas", true, Pascal, 29, 12, 6),
                arguments("perl.pl", true, Perl, 17, 7, 5),
                arguments("pcss.pcss", true, PostCss, 6, 5, 3),
                arguments("pest.pest", true, Pest, 4, 2, 2),
                arguments("php.php", true, Php, 10, 5, 1),
                arguments("poke.pk", true, Poke, 2, 1, 1),
                arguments("pom.xml", true, POM, 32, 4, 4),
                arguments("pony.pony", true, Pony, 3, 7, 2),
                arguments("postcss.sss", true, PostCss, 18, 4, 5),
                arguments("powershell.ps1", true, PowerShell, 9, 4, 4),
                arguments("processing.pde", true, Processing, 17, 9, 3),
                arguments("prolog.p", true, Prolog, 78, 55, 26),
                arguments("properties.properties", true, JavaProperties, 20, 31, 1),
                arguments("protobuf.proto", true, Protobuf, 27, 18, 11),
                arguments("psl.psl", true, PSL, 14, 15, 14),
                arguments("pug.pug", true, Pug, 8, 2, 1),
                arguments("puppet.pp", true, Puppet, 14, 2, 1),
                arguments("purescript.purs", true, PureScript, 14, 6, 3),
                arguments("python.py", true, Python, 5, 6, 1),
                arguments("python.py", false, Python, 10, 1, 1),
                arguments("q.q", true, Q, 5, 5, 4),
                arguments("qcl.qcl", true, Qcl, 113, 20, 26),
                arguments("qml.qml", true, Qml, 11, 4, 4),
                arguments("r.r", true, R, 68, 101, 94),
                arguments("racket.rkt", true, Racket, 15, 13, 11),
                arguments("Rakefile", true, Rakefile, 4, 1, 3),
                arguments("raku.raku", true, Raku, 18, 25, 6),
                arguments("raku.raku", false, Raku, 37, 6, 6),
                arguments("razor.cshtml", true, Razor, 8, 4, 4),
                arguments("redscript.reds", true, Redscript, 47, 19, 8),
                arguments("renpy.rpy", true, Renpy, 8, 8, 15),
                arguments("rescript.res", true, ReScript, 244, 24, 40),
                arguments("restructured.rst", true, ReStructuredText, 275, 0, 129),
                arguments("ron.ron", true, RON, 137, 6, 12),
                arguments("rpmspec.spec", true, RPMSpecfile, 22, 3, 15),
                arguments("ruby.rb", true, Ruby, 9, 7, 3),
                arguments("ruby_env", true, Ruby, 3, 5, 2),
                arguments("ruby_html.erb", true, RubyHtml, 21, 7, 5),
                arguments("rust.rs", true, Rust, 36, 3, 6, Markdown, 2, 0, 0),
                arguments("scala.scala", true, Scala, 32, 4, 14),
                arguments("scheme.scm", true, Scheme, 14, 4, 8),
                arguments("SConstruct", true, Scons, 3, 2, 4),
                arguments("scss.scss", true, Sass, 155, 5, 3),
                arguments("sed.sed", true, Sed, 2, 1, 1),
                arguments("sgml.sgml", true, Sgml, 8, 1, 3),
                arguments("shell.sh", true, Sh, 6, 2, 2),
                arguments("shaderlab.shader", true, ShaderLab, 31, 7, 4),
                arguments("slint.slint", true, Slint, 21, 2, 3),
                arguments("smalltalk.st", true, Smalltalk, 128, 151, 40),
                arguments("sml.sml", true, Sml, 10, 4, 0),
                arguments("solidity.sol", true, Solidity, 6, 6, 1),
                arguments("specmane.e", true, SpecmanE, 15, 7, 3),
                arguments("sqf.sqf", true, Sqf, 9, 12, 10),
                arguments("sql.sql", true, Sql, 4, 5, 3),
                arguments("srecode.srt", true, SRecode, 23, 2, 12),
                arguments("stan.stan", true, Stan, 123, 16, 2),
                arguments("stratego.str", true, Stratego, 12, 5, 6),
                arguments("stylus.styl", true, Stylus, 10, 4, 5),
                arguments("systemverilog.sv", true, SystemVerilog, 36, 6, 3),
                arguments("svelte.svelte", true, Svelte, 7, 6, 3, Css, 4, 9, 1, JavaScript, 4, 6, 1),
                arguments("svg.svg", true, Svg, 26, 3, 4),
                arguments("swift.swift", true, Swift, 6, 13, 3),
                arguments("swig.i", true, Swig, 8, 5, 3),
                arguments("tcl.tcl", true, Tcl, 27, 1, 1),
                arguments("Tera.tera", true, Tera, 26, 10, 5),
                arguments("tex.tex", true, Tex, 90, 2, 19),
                arguments("text.txt", true, Text, 3, 0, 1),
                arguments("thrift.thrift", true, Thrift, 29, 2, 7),
                arguments("toml.toml", true, Toml, 22, 3, 8),
                arguments("tsx.tsx", true, Tsx, 5, 3, 1),
                arguments("ttcn.ttcn3", true, Ttcn, 7, 5, 3),
                arguments("twig.twig", true, Twig, 14, 1, 1),
                arguments("typescript.ts", true, TypeScript, 20, 9, 3),
                arguments("example.umpl", true, UMPL, 58, 1, 7),
                arguments("unison.u", true, Unison, 6, 7, 2),
                arguments("unreal.uc", true, UnrealScript, 6, 4, 1),
                arguments("unreal.uproject", true, UnrealProject, 22, 0, 0),
                arguments("unreal.udn", true, UnrealDeveloperMarkdown, 53, 0, 33),
                arguments("unreal.uplugin", true, UnrealPlugin, 25, 0, 0),
                arguments("unreal.usf", true, UnrealShader, 77, 7, 14),
                arguments("unreal.ush", true, UnrealShaderHeader, 99, 10, 9),
                arguments("urweb.ur", true, UrWeb, 8, 4, 2),
                arguments("urweb_urp.urp", true, UrWebProject, 1, 1, 1),
                arguments("urweb_urs.urs", true, UrWeb, 1, 1, 1),
                arguments("vala.vala", true, Vala, 16, 6, 6),
                arguments("vb6_bas.bas", true, VB6, 6, 2, 3),
                arguments("vb6_cls.cls", true, VB6, 17, 2, 2),
                arguments("vb6_frm.frm", true, VB6, 29, 2, 2),
                arguments("vbscript.vbs", true, VBScript, 3, 2, 2),
                arguments("velocity.vm", true, Velocity, 4, 5, 2),
                arguments("verilog.vg", true, Verilog, 34, 5, 5),
                arguments("verilog.irunargs", true, VerilogArgsFile, 12, 0, 0),
                arguments("vhdl.vhd", true, Vhdl, 20, 6, 6),
                arguments("visualbasic.vb", true, VisualBasic, 4, 1, 1),
                arguments("VisualStudio.vcproj", true, VisualStudioProject, 172, 0, 0),
                arguments("VisualStudio.sln", true, VisualStudioSolution, 156, 1, 1),
                arguments("voice.vxml", true, VoiceXml, 38, 6, 1),
                arguments("vue.vue", true, Vue, 6, 1, 2, Css, 3, 3, 0, Html, 5, 1, 0, JavaScript, 10, 4, 1),
                arguments("webassembly.wat", true, WebAssembly, 8, 1, 1),
                arguments("wenyan.wy", true, WenYan, 107, 1, 33),
                arguments("wgsl.wgsl", true, WGSL, 10, 1, 1),
                arguments("wolfram.nb", true, Wolfram, 33, 3, 2),
                arguments("xaml.xaml", true, Xaml, 25, 5, 3),
                arguments("xcconfig.xcconfig", true, XcodeConfig, 4, 5, 3),
                arguments("xml.xml", true, Xml, 62, 3, 5),
                arguments("xsl.xsl", true, XSL, 7, 4, 2),
                arguments("xslfo.fob", true, XSLFO, 41, 1, 4),
                arguments("xtend.xtend", true, Xtend, 13, 4, 6),
                arguments("yaml.yaml", true, Yaml, 29, 2, 1),
                arguments("zencode.zs", true, ZenCode, 9, 6, 5),
                arguments("zig.zig", true, Zig, 5, 2, 2),
                arguments("shell.zsh", true, Zsh, 14, 12, 10)
        );
    }
}
